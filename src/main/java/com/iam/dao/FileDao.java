package com.iam.dao;

import com.iam.model.UserFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileDao extends JpaRepository<UserFile, Long> {
    List<UserFile> findByUserId(Long id);
    UserFile findByFileUrl(String fileUrl);
    List<UserFile> findByDiseaseName(String diseaseName);
}