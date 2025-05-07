package com.devstack.system.service.impl;

import com.devstack.system.dto.request.RequestSystemUserAvatarDto;
import com.devstack.system.entity.SystemUser;
import com.devstack.system.entity.SystemUserAvatar;
import com.devstack.system.exception.DuplicateEntryException;
import com.devstack.system.exception.EntryNotFoundException;
import com.devstack.system.exception.InternalServerException;
import com.devstack.system.repository.SystemUserAvatarRepo;
import com.devstack.system.repository.SystemUserRepo;
import com.devstack.system.service.FileService;
import com.devstack.system.service.SystemUserAvatarService;
import com.devstack.system.util.CommonFileSavedBinaryDataDTO;
import com.devstack.system.util.FileDataExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SystemUserAvatarServiceImpl implements SystemUserAvatarService {

    private final SystemUserAvatarRepo systemUserAvatarRepo;
    private final SystemUserRepo systemUserRepo;
    private final FileService fileService;
    private final FileDataExtractor fileDataExtractor;

    @Value("${bucketName}")
    private String bucketName;

    @Override
    public void createSystemUserAvatar(RequestSystemUserAvatarDto dto,String email, MultipartFile file) throws SQLException {
        CommonFileSavedBinaryDataDTO resource = null;
        Optional<SystemUser> selectedUser = systemUserRepo.findByEmail(email);
        if (selectedUser.isEmpty()) {
            throw new EntryNotFoundException("User not found.");
        }
        Optional<SystemUserAvatar> selectedAvatar = systemUserAvatarRepo.findByUserId(selectedUser.get().getPropertyId());
        if (selectedAvatar.isPresent()) {
            try {
                try {

                    // Delete the existing avatar resource directory
                    fileService.deleteResource(bucketName, "avatar/" + selectedUser.get().getPropertyId() + "/resource/",fileDataExtractor.byteArrayToString(selectedAvatar.get().getFileName()));
                } catch (Exception e) {
                    // Handle deletion failure if needed
                    throw new InternalServerException("Failed to delete existing avatar resource directory");
                }

                resource = fileService.createResource(file, "avatar/" + selectedUser.get().getPropertyId() + "/resource/", bucketName);

                selectedAvatar.get().setCreatedDate(dto.getCreatedDate());
                selectedAvatar.get().setDirectory(resource.getDirectory().getBytes());
                selectedAvatar.get().setFileName(fileDataExtractor.blobToByteArray(resource.getFileName()));
                selectedAvatar.get().setHash(fileDataExtractor.blobToByteArray(resource.getHash()));
                selectedAvatar.get().setResourceUrl(fileDataExtractor.blobToByteArray(resource.getResourceUrl()));

                systemUserAvatarRepo.save(selectedAvatar.get());

            } catch (Exception e) {
                assert resource != null;
                fileService.deleteResource(bucketName,
                        resource.getDirectory(), fileDataExtractor.extractActualFileName(
                                new InputStreamReader(
                                        resource.getFileName().getBinaryStream())));
                fileService.deleteResource(bucketName, "avatar/" + selectedUser.get().getPropertyId() + "/resource/",fileDataExtractor.byteArrayToString(selectedAvatar.get().getFileName()));
                throw new InternalServerException("Something went wrong");
            }
        } else {
            // save
            try {
                resource = fileService.createResource(file, "avatar/" + selectedUser.get().getPropertyId() + "/resource/", bucketName);
                SystemUserAvatar buildAvatar = SystemUserAvatar.builder()
                        .propertyId(UUID.randomUUID().toString())
                        .createdDate(dto.getCreatedDate())
                        .directory(resource.getDirectory().getBytes())
                        .fileName(fileDataExtractor.blobToByteArray(resource.getFileName()))
                        .hash(fileDataExtractor.blobToByteArray(resource.getHash()))
                        .resourceUrl(fileDataExtractor.blobToByteArray(resource.getResourceUrl()))
                        .systemUser(selectedUser.get()).build();
                systemUserAvatarRepo.save(buildAvatar);
            } catch (Exception e) {
                assert resource != null;
                fileService.deleteResource(bucketName,
                        resource.getDirectory(), fileDataExtractor.extractActualFileName(
                                new InputStreamReader(
                                        resource.getFileName().getBinaryStream())));
                throw new InternalServerException("Something went wrong");
            }
        }
    }
}
