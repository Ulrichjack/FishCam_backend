package com.fishcam.application.export;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostgresBackupService {


         @Value("${spring.datasource.username}")
         private String dbUsername;

         @Value("${spring.datasource.password}")
         private String dbPassword;

         @Value("${spring.datasource.url}")
         private String dbUrl;

         public File generateSqlBackup() throws  Exception{
             String dbName = dbUrl.substring(dbUrl.lastIndexOf("/") + 1);
             String filename = "fishcam_backup.sql";
             ProcessBuilder pb = new ProcessBuilder(
                     "pg_dump",
                     "-U", dbUsername,
                     "-f", filename,
                     dbName
             );

             pb.environment().put("PGPASSWORD", dbPassword);
             Process process = pb.start();
             int exitCode = process.waitFor();
             if (exitCode == 0){
                 return new  File(filename);
             }else{
                 throw new RuntimeException("Failed to generate SQL backup. Exit code: " + exitCode);

             }
         }

}
