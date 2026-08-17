package com.taskflow.projectservice.repository;

import com.taskflow.projectservice.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOwnerId(Long ownerId);
    List<Project> findByNameContainingIgnoreCase(String name);

}