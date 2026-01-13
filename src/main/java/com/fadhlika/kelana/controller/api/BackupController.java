package com.fadhlika.kelana.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fadhlika.kelana.dto.Response;
import com.fadhlika.kelana.service.BackupService;

@RestController
@RequestMapping("/api/v1/backups")
public class BackupController {
    @Autowired
    private BackupService backupService;

    @PostMapping
    public Response<Void> createBackup() {
        backupService.createBackup();

        return new Response<>("success");
    }
}
