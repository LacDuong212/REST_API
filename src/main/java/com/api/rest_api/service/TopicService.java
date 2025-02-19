package com.api.rest_api.service;

import com.api.rest_api.model.Topic;
import com.api.rest_api.repository.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class TopicService {
    @Autowired
    private TopicRepository topicRepository;

    public ResponseEntity<?> getAllTopics() {
        List<Topic> topics = topicRepository.findAll();
        return topics.isEmpty() ? ResponseEntity.ok("Không có topic để hiển thị") : ResponseEntity.ok(topics);
    }
}
