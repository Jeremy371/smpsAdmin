package com.humane.smps.controller.admin;

import com.blogspot.na5cent.exom.ExOM;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humane.smps.dto.UploadExamineeDto;
import com.humane.smps.dto.UploadHallDto;
import com.humane.smps.dto.UploadItemDto;
import com.humane.smps.model.*;
import com.humane.smps.repository.*;
import com.humane.smps.service.SettingService;
import com.humane.util.jasperreports.JasperReportsExportHelper;
import com.mysema.query.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SettingController {
    private final SettingService settingService;
    private final AdmissionRepository admissionRepository;
    private final ExamRepository examRepository;
    private final HallRepository hallRepository;
    private final ExamHallRepository examHallRepository;
    private final ExamineeRepository examineeRepository;
    private final ExamMapRepository examMapRepository;

    @RequestMapping(value = "uploadItem", method = RequestMethod.POST)
    public ResponseEntity uploadItem(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        // 파일이 없울경우 에러 리턴.
        if (multipartFile.isEmpty()) return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(null);

        File tempFile = File.createTempFile("test", ".tmp");
        multipartFile.transferTo(tempFile);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            // 1. excel 변환
            List<UploadItemDto> itemList = ExOM.mapFromExcel(tempFile).to(UploadItemDto.class).map(1);
            for (UploadItemDto dto : itemList) {
                // 2. admission 변환, 저장
                Admission admission = mapper.convertValue(dto, Admission.class);
                admission = admissionRepository.save(admission);

                // 3. exam 변환, 저장
                Exam exam = mapper.convertValue(dto, Exam.class);
                exam.setAdmission(admission);
                examRepository.save(exam);

                // 4. item 변환, 저장, 갯수비교
                if (Long.parseLong(dto.getItemCnt()) != settingService.saveItems(dto)) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("항목 갯수가 일치하지 않습니다!.");
                }
            }
            return ResponseEntity.ok("데이터 정상 처리 완료");
        } catch (Throwable t) {
            t.printStackTrace();
            log.error("{}", t.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("데이터에러. 서버 로그 확인 바람.");
        } finally {
            tempFile.delete();
        }
    }

    @RequestMapping(value = "uploadHall", method = RequestMethod.POST)
    public void uploadHall(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("test", ".tmp");
        multipartFile.transferTo(tempFile);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            List<UploadHallDto> hallList = ExOM.mapFromExcel(tempFile).to(UploadHallDto.class).map(1);
            hallList.forEach(uploadHallDto -> {
                /**
                 * 제약조건 : 시험정보는 반드시 업로드 되어 있어야 한다.
                 */

                // 1. 시험정보 생성
                Exam exam = mapper.convertValue(uploadHallDto, Exam.class);
                exam = examRepository.findOne(exam.getExamCd());

                // 2. 고사실정보 생성
                Hall hall = mapper.convertValue(uploadHallDto, Hall.class);
                hall = hallRepository.save(hall);

                // 3. 응시고사실 생성
                ExamHall examHall = new ExamHall();
                examHall.setExam(exam);
                examHall.setHall(hall);

                // 4. 응시고사실 저장
                examHallRepository.save(examHall);
            });
        } catch (Throwable throwable) {
            log.error("{}", throwable.getMessage());
        } finally {
            tempFile.delete();
        }
    }

    @RequestMapping(value = "uploadExaminee", method = RequestMethod.POST)
    public void uploadExaminee(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("test", ".tmp");
        multipartFile.transferTo(tempFile);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

        try {
            List<UploadExamineeDto> examineeList = ExOM.mapFromExcel(tempFile).to(UploadExamineeDto.class).map(1);
            examineeList.forEach(vo -> {
                log.debug("{}", vo);

                // 1. ExamHall 에서 고사실 및 시험정보를 가져온다.
                QExam exam = QExamHall.examHall.exam;
                QHall hall = QExamHall.examHall.hall;

                BooleanBuilder builder = new BooleanBuilder()
                        .and(exam.examDate.eq(dtf.parseLocalDateTime(vo.getExamDate()).toDate()))
                        .and(exam.examTime.eq(dtf.parseLocalDateTime(vo.getExamTime()).toDate()))
                        .and(hall.headNm.eq(vo.getHeadNm()))
                        .and(hall.bldgNm.eq(vo.getBldgNm()))
                        .and(hall.hallNm.eq(vo.getHallNm()));

                log.debug("{}", builder.toString());

                ExamHall examHall = examHallRepository.findOne(builder);
                log.debug("{}", examHall);

                // 3. 수험생정보 생성
                Examinee examinee = mapper.convertValue(vo, Examinee.class);
                examineeRepository.save(examinee);

                ExamMap examMap = new ExamMap();
                examMap.setExam(examHall.getExam());
                examMap.setHall(examHall.getHall());
                examMap.setExaminee(examinee);

                // 3.1 수험생정보 저장
                examMapRepository.save(examMap);


            });
        } catch (Throwable throwable) {
            log.error("{}", throwable.getMessage());
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