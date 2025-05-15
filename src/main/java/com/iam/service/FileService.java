package com.iam.service;

import com.iam.dao.FileDao;
import com.iam.dto.FileDTO;
import com.iam.model.UserFile;
import com.iam.model.User;
import com.iam.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Service
public class FileService {
    @Autowired
    private FileDao fileDao;

    @Autowired
    private UserService userService;

    public FileDTO saveFile(MultipartFile file, User user, String diseaseName, String confidence) {
        String uploadDir = Constants.FILE_PATH;
        Path uploadPath = Path.of(uploadDir);
        try{
            if(!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }
            String fileExtension = "";
            if(file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")){
                fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            }
            String fileName = diseaseName + "_" + System.currentTimeMillis() + fileExtension;
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            UserFile newUserFile = new UserFile();
            newUserFile.setUser(user);
            newUserFile.setDiseaseName(diseaseName);
            newUserFile.setConfidence(confidence);
            newUserFile.setFileUrl(fileName);
            newUserFile.setCreatedAt(new Date());
            UserFile savedUserFile = fileDao.save(newUserFile);
            return createFileDTO(savedUserFile);
        }catch (IOException e){
            throw new RuntimeException("Failed to save file");
        }
    }
    public FileDTO createFileDTO(UserFile userFile){
        FileDTO fileDTO = new FileDTO();
        fileDTO.setDiseaseName(userFile.getDiseaseName());
        fileDTO.setConfidence(userFile.getConfidence());
        fileDTO.setFileUrl(userFile.getFileUrl());
        fileDTO.setCreatedAt(userFile.getCreatedAt());
        return fileDTO;
    }

    public List<FileDTO> getFilesByUserId(Long userId) {
        List<UserFile> userFile = fileDao.findByUserId(userId);
        return userFile.stream().map(this::createFileDTO).toList();
    }

    public byte[] getFileByFilePath(String fileUrl)  {
        UserFile userFile = fileDao.findByFileUrl(fileUrl);
        try{
            if(userFile != null){
                String filePath = Constants.FILE_PATH + userFile.getFileUrl();
                return Files.readAllBytes(Path.of(filePath));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file");
        }
        return null;
    }
}
