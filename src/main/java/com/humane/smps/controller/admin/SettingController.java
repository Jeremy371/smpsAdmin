package com.humane.smps.controller.admin;

import com.blogspot.na5cent.exom.ExOM;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humane.smps.dto.UploadExamineeDto;
import com.humane.smps.dto.UploadHallDto;
import com.humane.smps.dto.UploadItemDto;
import com.humane.smps.model.Exam;
import com.humane.util.jasperreports.JasperReportsExportHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "setting")
@Slf4j
public class SettingController {

    @RequestMapping(value = "uploadItem", method = RequestMethod.POST)
    public void uploadItem(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("test", ".tmp");
        multipartFile.transferTo(tempFile);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            List<UploadItemDto> itemList = ExOM.mapFromExcel(tempFile).to(UploadItemDto.class).map(1);
            itemList.forEach(uploadItemDto -> {
                log.debug("{}", uploadItemDto);
                Exam a = mapper.convertValue(uploadItemDto, Exam.class);
                log.debug("{}", a);
            });

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            tempFile.delete();
        }
    }

    @RequestMapping(value = "uploadHall", method = RequestMethod.POST)
    public void uploadHall(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("test", ".tmp");
        multipartFile.transferTo(tempFile);

        try {
            List<UploadHallDto> hallList = ExOM.mapFromExcel(tempFile).to(UploadHallDto.class).map(1);
            hallList.forEach(uploadHallDto -> log.debug("{}", uploadHallDto));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            tempFile.delete();
        }
    }

    @RequestMapping(value = "uploadExaminee", method = RequestMethod.POST)
    public void uploadExaminee(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("test", ".tmp");
        multipartFile.transferTo(tempFile);

        try {
            List<UploadExamineeDto> examineeList = ExOM.mapFromExcel(tempFile).to(UploadExamineeDto.class).map(1);
            examineeList.forEach(uploadExamineeDto -> log.debug("{}", uploadExamineeDto));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            tempFile.delete();
        }
    }

    @RequestMapping(value = "downloadItem", method = RequestMethod.GET)
    public ResponseEntity downloadItem(HttpServletResponse response) {
        return JasperReportsExportHelper.toResponseEntity(response,
                "jrxml/setting-item.jrxml",
                "xlsx",
                null
        );
    }

    @RequestMapping(value = "downloadHall", method = RequestMethod.GET)
    public ResponseEntity downloadHall(HttpServletResponse response) {
        return JasperReportsExportHelper.toResponseEntity(response,
                "jrxml/setting-hall.jrxml",
                "xlsx",
                null
        );
    }

    @RequestMapping(value = "downloadExaminee", method = RequestMethod.GET)
    public ResponseEntity downloadExaminee(HttpServletResponse response) {
        return JasperReportsExportHelper.toResponseEntity(response,
                "jrxml/setting-examinee.jrxml",
                "xlsx",
                null
        );
    }
}