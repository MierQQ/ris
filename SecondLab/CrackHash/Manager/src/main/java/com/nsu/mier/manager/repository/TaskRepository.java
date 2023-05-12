package com.nsu.mier.manager.repository;

import com.nsu.mier.manager.type.MongoTaskStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TaskRepository extends MongoRepository<MongoTaskStatus, String> {

}
