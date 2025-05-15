package com.iam.controller;

import com.iam.dto.FileDTO;
import com.iam.model.User;
import com.iam.service.FileService;
import com.iam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/files")
public class FileController {
    @Autowired
    private FileService fileService;
    
    @Autowired
    private UserService userService;

    @PostMapping("/{userId}")
    public ResponseEntity<?> uploadFile(@RequestParam("file")MultipartFile file, @PathVariable("userId") Long id, @RequestParam("diseaseName") String diseaseName, @RequestParam("confidence") String confidence){
        if(Objects.isNull(file) || file.isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("message","File is mandatory"));
        }
        if(Objects.isNull(id)){
            return ResponseEntity.badRequest().body(Map.of("message","User ID is mandatory"));
        }
        User user = userService.getUserById(id);
        if(Objects.isNull(user)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","User not found"));
        }
        FileDTO savedFile = fileService.saveFile(file, user, diseaseName, confidence);
        return ResponseEntity.ok(savedFile);
    }

    @GetMapping("filePath/{filePath}")
    public ResponseEntity<?> getFile(@PathVariable("filePath") String filePath) {
        if(Objects.isNull(filePath)){
            return ResponseEntity.badRequest().body(Map.of("message","File ID is mandatory"));
        }
            // Get the file
            byte[] fileContent = fileService.getFileByFilePath(filePath);
            if(fileContent == null || fileContent.length == 0){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","File not found"));
            }
            String mediaType = getFileMediaType(filePath);

            // Return the file as a response with proper content type
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mediaType))
                .contentLength(fileContent.length)
                .body(fileContent);
        }

    private String getFileMediaType(String filePath) {
        String mediaType = "image/jpeg"; // default to JPEG
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
            mediaType = "image/jpeg";
        }
        if (filePath.endsWith(".png")) {
            mediaType = "image/png";
        }
            return mediaType;
    }


    @GetMapping("/{userId}")
    public ResponseEntity<?> getFiles(@PathVariable("userId") Long id){
        if(Objects.isNull(id)){
            return ResponseEntity.badRequest().body(Map.of("message","User ID is mandatory"));
        }
        User user = userService.getUserById(id);
        if(Objects.isNull(user)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","User not found"));
        }
        List<FileDTO> files = fileService.getFilesByUserId(id);
        return ResponseEntity.ok(files);
    }
}
