package com.api.rest_api.service;

import com.api.rest_api.dto.*;
import com.api.rest_api.dto.QuizResponseDTO;
import com.api.rest_api.model.*;
import com.api.rest_api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class QuizService {
    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AttemptRepository attemptRepository;

    @Autowired
    private QuizResponseRepository quizResponseRepository;

    @Autowired
    private CoinHistoryRepository coinHistoryRepository;

    public QuizResponseDTO getQuizById(Long qid) {
        Quiz quiz = quizRepository.findById(qid)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        QuizResponseDTO response = new QuizResponseDTO();
        response.setQid(quiz.getQid());
        response.setTitle(quiz.getTitle());
        response.setQuestions(getQuestionResponses(quiz.getQuestions()));
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

    public ResponseEntity<?> saveQuiz(QuizRequest quizRequest) {
        Quiz quiz;

        if (quizRequest.getQid() == -1L) {
            // Create new quiz with 6-digit ID
            long sixDigitId;
            do {
                sixDigitId = 100000 + new Random().nextInt(900000);
            } while (quizRepository.existsById(sixDigitId));

            quiz = new Quiz();
            quiz.setQid((long) sixDigitId);
            quiz.setCreatedDate(LocalDate.now()); // set default
        } else {
            // Try to fetch the existing quiz
            quiz = quizRepository.findById(quizRequest.getQid())
                    .orElseGet(() -> {
                        Quiz newQuiz = new Quiz();
                        newQuiz.setQid(quizRequest.getQid());
                        newQuiz.setCreatedDate(LocalDate.now());
                        return newQuiz;
                    });
        }

        // Apply common updates
        quiz.setTitle(quizRequest.getTitle());
        quiz.setDescription(quizRequest.getDescription());
        quiz.setTopic(quizRequest.getTopic());
        quiz.setPublic(quizRequest.isPublic());
        quiz.setDuration(quizRequest.getDuration());

        quizRepository.save(quiz);
        return ResponseEntity.ok("Quiz saved successfully!");
    }
}
