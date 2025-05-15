package com.api.rest_api.service;

import com.api.rest_api.dto.AnswerModel;
import com.api.rest_api.dto.QuestionModel;
import com.api.rest_api.dto.QuizEditorRequest;
import com.api.rest_api.dto.QuizModel;
import com.api.rest_api.model.*;
import com.api.rest_api.repository.*;
import com.api.rest_api.strings.AclRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.*;

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
        quiz.setPublic(quizModel.isMPublic());
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
        quiz.setPublic(quizModel.isMPublic());
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
}
