package com.api.rest_api.service;

import com.api.rest_api.model.*;
import com.api.rest_api.repository.*;
import com.api.rest_api.strings.AclRole;
import com.api.rest_api.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class QuizService {
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private AnswerRepository answerRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ACLRepository aclRepository;

    @Autowired
    private AttemptRepository attemptRepository;

    @Autowired
    private QuizResponseRepository quizResponseRepository;

    @Autowired
    private CoinHistoryRepository coinHistoryRepository;

    @Autowired
    private LobbyRepository lobbyRepository;

    @Autowired
    private LobbyParticipantRepository lobbyParticipantRepository;


    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Tạo lobby
    public LobbyResponse createLobby(LobbyRequest request) {
        Quiz quiz = quizRepository.findById(request.getQid())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        Account creator = accountRepository.findById(request.getUid())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String code;
        do {
            code = String.format("%06d", new Random().nextInt(999999));
        } while (lobbyRepository.findByCode(code).isPresent());

        Lobby lobby = new Lobby();
        lobby.setCode(code);
        lobby.setQuiz(quiz);
        lobby.setStatus("PENDING");
        lobby = lobbyRepository.save(lobby);

        LobbyParticipant participant = new LobbyParticipant();
        participant.setLobby(lobby);
        participant.setAccount(creator);
        participant.setScore(0);
        lobbyParticipantRepository.save(participant);

        LobbyResponse response = new LobbyResponse();
        response.setLid(lobby.getLid());
        response.setCode(lobby.getCode());
        return response;
    }

    // Tham gia lobby
    public void joinLobby(JoinLobbyRequest request) {
        Lobby lobby = lobbyRepository.findByCode(request.getCode())
                .orElseThrow(() -> new RuntimeException("Lobby not found"));
        Account account = accountRepository.findById(request.getUid())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (lobby.getStatus().equals("STARTED") || lobby.getStatus().equals("FINISHED")) {
            throw new RuntimeException("Lobby is not accepting new players");
        }

        LobbyParticipant participant = new LobbyParticipant();
        participant.setLobby(lobby);
        participant.setAccount(account);
        participant.setScore(0);
        lobbyParticipantRepository.save(participant);

        // Thông báo qua WebSocket
        messagingTemplate.convertAndSend("/topic/lobby/" + lobby.getLid(), getLobbyStatus(lobby.getLid()));
    }

    // Get lobby info
    public LobbyResponse getLobbyInfo(Long lid) {
        Lobby lobby = lobbyRepository.findById(lid)
                .orElseThrow(() -> new RuntimeException("Lobby not found"));

        LobbyResponse response = new LobbyResponse();
        response.setLid(lobby.getLid());
        response.setCode(lobby.getCode());
        response.setQid(lobby.getQuiz().getQid());
        return response;
    }

    // Bắt đầu game
    public void startLobby(Long lid, Long uid) {
        Lobby lobby = lobbyRepository.findById(lid)
                .orElseThrow(() -> new RuntimeException("Lobby not found"));
        Account account = accountRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Kiểm tra quyền chủ phòng (giả định người tạo là chủ)
        LobbyParticipant creator = lobbyParticipantRepository.findByLobbyLid(lid).stream()
                .filter(p -> p.getAccount().getUid().equals(uid))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Not authorized"));

        lobby.setStatus("STARTED");
        lobby.setStartTime(LocalDateTime.now());
        lobby.setCurrentQuestionIndex(0); // Bắt đầu từ câu hỏi đầu tiên
        lobbyRepository.save(lobby);

        // Gửi câu hỏi đầu tiên
        sendNextQuestion(lid, 0);
    }

    // Xử lý câu trả lời
    public void submitAnswer(QuizAnswerRequest request) {
        System.out.println("Processing answer: lid=" + request.getLid() + ", uid=" + request.getUid() +
                          ", qtid=" + request.getQtid() + ", aid=" + request.getAid());

        Lobby lobby = lobbyRepository.findById(request.getLid())
                .orElseThrow(() -> new RuntimeException("Lobby not found"));
        System.out.println("Found lobby: " + lobby.getLid() + ", status=" + lobby.getStatus() +
                          ", currentQuestionIndex=" + lobby.getCurrentQuestionIndex());

        LobbyParticipant participant = lobbyParticipantRepository.findByLobbyLid(lobby.getLid()).stream()
                .filter(p -> p.getAccount().getUid().equals(request.getUid()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Participant not found"));
        System.out.println("Found participant: " + participant.getAccount().getUsername());

        // Kiểm tra câu trả lời - Tải Quiz với các Questions trong cùng một transaction
        Question question = null;

        // Thay vì sử dụng lazy-loaded questions từ lobby.getQuiz(), tải lại quiz với questions
        Quiz quiz = quizRepository.findById(lobby.getQuiz().getQid())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        System.out.println("Quiz: " + quiz.getQid() + ", title=" + quiz.getTitle());

        // Tải câu hỏi và câu trả lời từ repository
        question = questionRepository.findById(request.getQtid())
                .orElseThrow(() -> new RuntimeException("Question not found"));
        System.out.println("Found question: " + question.getQtid() + ", text=" + question.getQuestion());

        boolean isCorrect = false;
        if (question != null && request.getAid() != null) {
            // Tải danh sách đáp án từ repository
            List<Answer> answers = answerRepository.findByQuestion(question);
            isCorrect = answers.stream()
                    .filter(a -> a.getAid().equals(request.getAid()))
                    .anyMatch(Answer::isCorrect);
            System.out.println("Answer is correct: " + isCorrect);
        }

        if (isCorrect) {
            participant.setScore(participant.getScore() + 10); // Cộng 10 điểm
            lobbyParticipantRepository.save(participant);
            System.out.println("Updated score for " + participant.getAccount().getUsername() + ": " + participant.getScore());
        }

        // Cập nhật bảng xếp hạng
        LobbyStatus rankingStatus = getLobbyStatus(lobby.getLid());
        messagingTemplate.convertAndSend("/topic/lobby/" + lobby.getLid(), rankingStatus);
        System.out.println("Sent ranking update");

        // Đếm số người đã trả lời câu hỏi hiện tại
        int totalParticipants = lobbyParticipantRepository.findByLobbyLid(lobby.getLid()).size();
        System.out.println("Total participants: " + totalParticipants);

        // Nếu tất cả người chơi đã trả lời hoặc đây là câu trả lời cuối cùng, chuyển đến câu hỏi tiếp theo
        int currentIndex = lobby.getCurrentQuestionIndex();

        // Lấy tổng số câu hỏi từ repository
        int totalQuestions = questionRepository.countByQuiz(quiz);
        System.out.println("Current question index: " + currentIndex + ", total questions: " + totalQuestions);

        // Force chuyển câu hỏi
        // Nếu còn câu hỏi tiếp theo
        if (currentIndex < totalQuestions - 1) {
            // Tăng chỉ số câu hỏi và lưu lại
            currentIndex++;
            lobby.setCurrentQuestionIndex(currentIndex);
            lobbyRepository.save(lobby);
            System.out.println("Moving to next question: " + currentIndex);

            // Gửi câu hỏi tiếp theo
            sendNextQuestion(lobby.getLid(), currentIndex);
        } else {
            // Đã hết câu hỏi, kết thúc game
            lobby.setStatus("FINISHED");
            lobbyRepository.save(lobby);
            System.out.println("Game finished");

            // Gửi thông báo kết thúc
            LobbyStatus finalStatus = getLobbyStatus(lobby.getLid());
            messagingTemplate.convertAndSend("/topic/lobby/" + lobby.getLid(), finalStatus);
            System.out.println("Sent game finished notification");
        }
    }

    private void sendNextQuestion(Long lid, int questionIndex) {
        System.out.println("Sending next question for lobby " + lid + ", index: " + questionIndex);

        Lobby lobby = lobbyRepository.findById(lid)
                .orElseThrow(() -> new RuntimeException("Lobby not found"));
        System.out.println("Found lobby: " + lobby.getLid() + ", status: " + lobby.getStatus());

        // Lấy danh sách câu hỏi từ repository thay vì từ quiz
        Quiz quiz = lobby.getQuiz();
        List<Question> questions = questionRepository.findByQuiz(quiz);
        System.out.println("Quiz has " + questions.size() + " questions");

        if (questionIndex >= 0 && questionIndex < questions.size()) {
            Question nextQuestion = questions.get(questionIndex);
            System.out.println("Next question: " + nextQuestion.getQtid() + ", " + nextQuestion.getQuestion());
        }

        // Gửi câu hỏi với chỉ số cụ thể
        LobbyStatus status = getLobbyStatus(lid);
        status.setCurrentQuestionIndex(questionIndex);
        status.setQid(lobby.getQuiz().getQid());

        System.out.println("Sending lobby status with question index: " + status.getCurrentQuestionIndex());

        // Gửi thông báo tới tất cả người chơi
        messagingTemplate.convertAndSend("/topic/lobby/" + lobby.getLid(), status);
        System.out.println("Status sent to /topic/lobby/" + lobby.getLid());
    }

    private LobbyStatus getLobbyStatus(Long lid) {
        Lobby lobby = lobbyRepository.findById(lid)
                .orElseThrow(() -> new RuntimeException("Lobby not found"));
        List<LobbyParticipant> participants = lobbyParticipantRepository.findByLobbyLid(lid);
        LobbyStatus status = new LobbyStatus();
        status.setLid(lid);
        status.setStatus(lobby.getStatus());
        status.setQid(lobby.getQuiz().getQid());
        status.setCurrentQuestionIndex(lobby.getCurrentQuestionIndex());
        status.setParticipants(participants.stream().map(p -> {
            ParticipantStatus ps = new ParticipantStatus();
            ps.setUid(p.getAccount().getUid());
            ps.setUsername(p.getAccount().getUsername());
            ps.setScore(p.getScore());
            return ps;
        }).collect(Collectors.toList()));
        return status;
    }

    public QuizResponseDTO getQuizById(Long qid) {
        Quiz quiz = quizRepository.findById(qid)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // Lấy danh sách câu hỏi từ repository thay vì từ quiz
        List<Question> questions = questionRepository.findByQuiz(quiz);

        QuizResponseDTO response = new QuizResponseDTO();
        response.setQid(quiz.getQid());
        response.setTitle(quiz.getTitle());
        response.setDuration(quiz.getDuration());
        response.setQuestions(getQuestionResponses(questions));
        return response;
    }

    private List<QuestionResponse> getQuestionResponses(List<Question> questions) {
        return questions.stream().map(q -> {
            QuestionResponse qr = new QuestionResponse();
            qr.setQtid(q.getQtid());
            qr.setQuestion(q.getQuestion());
            qr.setType(q.getType());
            qr.setAnswers(getAnswerResponses(q.getAnswers()));
            return qr;
        }).collect(Collectors.toList());
    }

    private List<AnswerResponse> getAnswerResponses(List<Answer> answers) {
        return answers.stream().map(a -> {
            AnswerResponse ar = new AnswerResponse();
            ar.setAid(a.getAid());
            ar.setText(a.getText());
            ar.setIsCorrect(a.isCorrect());
            System.out.println("Answer aid=" + a.getAid() + ", text=" + a.getText() + ", isCorrect=" + a.isCorrect()); // Debug log
            return ar;
        }).collect(Collectors.toList());
    }

    public AttemptResponse createAttempt(AttemptRequest request) {
        Attempt attempt = new Attempt();
        attempt.setScore(request.getScore().floatValue());
        attempt.setSubmitTime(Timestamp.valueOf(LocalDateTime.parse(request.getSubmitTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        attempt.setAttemptTime(request.getAttemptTime());
        if (request.getUid() != null) {
            Account account = accountRepository.findById(request.getUid())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            attempt.setAccount(account);
        }
        Quiz quiz = quizRepository.findById(request.getQid())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        attempt.setQuiz(quiz);
        attempt = attemptRepository.save(attempt);
        AttemptResponse response = new AttemptResponse();
        response.setAtid(attempt.getAtid());
        return response;
    }

    public void createQuizResponse(QuizResponseRequest request) {
        QuizResponse response = new QuizResponse();
        response.setAnswer(request.getAnswer());
        Attempt attempt = attemptRepository.findById(request.getAtid())
                .orElseThrow(() -> new RuntimeException("Attempt not found"));
        response.setAttempt(attempt);
        Question question = quizRepository.findById(attempt.getQuiz().getQid())
                .flatMap(q -> q.getQuestions().stream().filter(qt -> qt.getQtid().equals(request.getQtid())).findFirst())
                .orElseThrow(() -> new RuntimeException("Question not found"));
        response.setQuestion(question);
        quizResponseRepository.save(response);
    }

    public void createCoinHistory(CoinHistoryRequest request) {
        if (request.getTransactionTime() == null) {
            throw new IllegalArgumentException("transactionTime cannot be null");
        }

        CoinHistory history = new CoinHistory();
        Account account = accountRepository.findById(request.getUid())
                .orElseThrow(() -> new RuntimeException("User not found"));
        history.setAccount(account);
        history.setAmount(request.getCoins());
        history.setDescription(request.getDescription());

        try {
            // Định dạng khớp với frontend: yyyy-MM-dd'T'HH:mm:ss
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            history.setTimestamp(LocalDateTime.parse(request.getTransactionTime(), formatter));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid transactionTime format: " + request.getTransactionTime(), e);
        }

        coinHistoryRepository.save(history);
    }

//    public ResponseEntity<?> getQuizzesByTopicId(Long topicId) {
//        List<Quiz> quizzes = quizRepository.findByTopic_Tid(topicId);
//        return quizzes.isEmpty() ? ResponseEntity.ok("Không có quiz nào cho topic này!") : ResponseEntity.ok(quizzes);
//    }

    public ResponseEntity<?> getTop10QuizzesByAttempts() {
        List<Quiz> quizzesTop10 = quizRepository.findTop10ByMostAttempts(PageRequest.of(0, 10));
        return quizzesTop10.isEmpty() ? ResponseEntity.ok("Không có quiz nào phổ biến!") : ResponseEntity.ok(quizzesTop10);
    }

    public ResponseEntity<?> getQuizzesCreatedPastWeek() {
        LocalDate oneWeekAgo = LocalDate.now().minusWeeks(1);
        List<Quiz> quizzesPastWeek = quizRepository.findQuizzesCreatedPastWeek(oneWeekAgo, PageRequest.of(0, 10));
        return quizzesPastWeek.isEmpty() ? ResponseEntity.ok("Không tìm thấy quiz nào tạo trong tuần vừa rồi!") : ResponseEntity.ok(quizzesPastWeek);
    }

    public ResponseEntity<?> createQuiz(QuizEditorRequest quizEditorRequest) {
        QuizModel quizModel = quizEditorRequest.getQuiz();

        if (quizModel.getQid() != -1L) {
            return ResponseEntity.badRequest().body("Quiz không hợp lệ!");
        }

        Optional<Account> optionalAccount = accountRepository.findById(quizModel.getUid());
        if (optionalAccount.isEmpty()) {
            return ResponseEntity.badRequest().body("Account không tồn tại!");
        }
        Account creator = optionalAccount.get();

        long sixDigitId;
        do {
            sixDigitId = 100000 + new Random().nextInt(900000);
        } while (quizRepository.existsById(sixDigitId));

        Quiz quiz = new Quiz();
        quiz.setQid(sixDigitId);
        quiz.setCreatedDate(LocalDate.now());
        quiz.setTitle(quizModel.getTitle());
        quiz.setDescription(quizModel.getDescription());
        quiz.setTopic(quizModel.getTopic());
        quiz.setPublic(quizModel.isVisible());
        quiz.setDuration(quizModel.getDuration());

        List<Question> questionEntities = new ArrayList<>();
        for (QuestionModel qm : quizEditorRequest.getQuestions()) {
            if (qm.getQuestion() == null || qm.getQuestion().isEmpty()  || qm.getAnswers() == null) continue;

            List<AnswerModel> validAnswers = qm.getAnswers().stream()
                    .filter(a -> a.getText() != null && !a.getText().isEmpty())
                    .toList();

            if (validAnswers.size() < 2) continue;

            Question question = new Question();
            question.setQuestion(qm.getQuestion());
            question.setType(qm.getType());
            question.setQuiz(quiz);

            for (AnswerModel am : validAnswers) {
                Answer answer = new Answer();
                answer.setText(am.getText());
                answer.setCorrect(am.isCorrect());
                answer.setQuestion(question);
                question.getAnswers().add(answer);
            }

            questionEntities.add(question);
        }

        ACL acl = new ACL();
        acl.setAccount(creator);
        acl.setQuiz(quiz);
        acl.setRole(AclRole.OWNER);

        List<ACL> aclList = new ArrayList<>();
        aclList.add(acl);

        creator.getAclRoles().add(acl);

        quiz.setQuestions(questionEntities);
        quiz.setAclRoles(aclList);

        quizRepository.save(quiz); // saves all questions, answers, ACL

        return ResponseEntity.ok(Collections.singletonMap("NEW_QID", quiz.getQid()));
    }

    public ResponseEntity<?> updateQuiz(QuizEditorRequest quizEditorRequest) {
        QuizModel quizModel = quizEditorRequest.getQuiz();

        Optional<Quiz> optionalQuiz = quizRepository.findById(quizModel.getQid());
        if (optionalQuiz.isEmpty()) {
            return ResponseEntity.badRequest().body("Quiz không tồn tại!");
        }
        Quiz quiz = optionalQuiz.get();

        quiz.setTitle(quizModel.getTitle());
        quiz.setDescription(quizModel.getDescription());
        quiz.setTopic(quizModel.getTopic());
        quiz.setPublic(quizModel.isVisible());
        quiz.setDuration(quizModel.getDuration());

        if (quiz.getQuestions() != null) {
            quiz.getQuestions().clear();        // orphan removal
        } else {
            quiz.setQuestions(new ArrayList<>());
        }

        for (QuestionModel qm : quizEditorRequest.getQuestions()) {
            if (qm.getQuestion() == null || qm.getQuestion().isEmpty() || qm.getAnswers() == null) return null;

            List<AnswerModel> validAnswers = qm.getAnswers().stream()
                    .filter(a -> a.getText() != null && !a.getText().isEmpty())
                    .toList();

            if (validAnswers.size() < 2) return null;

            Question question = new Question();
            question.setQuestion(qm.getQuestion());
            question.setType(qm.getType());
            question.setQuiz(quiz);

            for (AnswerModel am : validAnswers) {
                Answer answer = new Answer();
                answer.setText(am.getText());
                answer.setCorrect(am.isCorrect());
                answer.setQuestion(question);
                question.getAnswers().add(answer);
            }

            quiz.getQuestions().add(question);
        }

        quizRepository.save(quiz); // will delete orphans and save new questions/answers

        return ResponseEntity.ok("Update quiz successfully!");
    }

    public ResponseEntity<?> getQuizEditorByQid(@PathVariable Long qid,
            @RequestParam Long uid) {

        boolean hasAccess = aclRepository.existsByAccount_UidAndQuiz_Qid(uid, qid);

        if (!hasAccess) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền sửa quiz này!");
        }

        Optional<Quiz> optionalQuiz = quizRepository.findById(qid);
        if (!optionalQuiz.isPresent()) {
            return ResponseEntity.badRequest().body("Quiz không tồn tại!");
        }

        Quiz quiz = optionalQuiz.get();

        QuizModel quizModel = new QuizModel(quiz);
        List<QuestionModel> questionModels = quiz.getQuestions().stream().map(QuestionModel::new).toList();
        QuizEditorRequest response = new QuizEditorRequest(quizModel, questionModels);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> getQuizzesCreatedByUid(Long uid) {
        if (!accountRepository.existsByUid(uid)) {
            return ResponseEntity.badRequest().body("Tài khoản không tồn tại!");
        }

        List<Quiz> quizzes = quizRepository.findQuizzesCreatedByUid(uid);
        if (quizzes.isEmpty()) {
            return ResponseEntity.ok("Không tìm thấy quiz nào được tạo bởi tài khoản này!");
        }

        List<QuizModel> quizModels = quizzes.stream().map(QuizModel::new).collect(Collectors.toList());

        return ResponseEntity.ok(quizModels);
    }

    public ResponseEntity<?> getQuizzesAttemptedByUid(Long uid) {
        if (!accountRepository.existsByUid(uid)) {
            return ResponseEntity.badRequest().body("Tài khoản không tồn tại!");
        }

        List<Quiz> quizzes = quizRepository.findQuizzesAttemptedByUid(uid);
        if (quizzes.isEmpty()) {
            return ResponseEntity.ok("Không tìm thấy quiz nào đã được làm bởi tài khoản này!");
        }

        List<QuizModel> quizModels = quizzes.stream().map(QuizModel::new).collect(Collectors.toList());

        return ResponseEntity.ok(quizModels);
    }
}
