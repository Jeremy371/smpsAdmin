package com.humane.smps.controller;

import com.humane.smps.dto.*;
import com.humane.smps.mapper.DataMapper;
import com.humane.smps.mapper.StatusMapper;
import com.humane.smps.service.DataService;
import com.humane.util.file.FileNameEncoder;
import com.humane.util.file.FileUtils;
import com.humane.util.jasperreports.JasperReportsExportHelper;
import com.humane.util.zip4j.ZipFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.exception.ZipException;
import net.sf.dynamicreports.report.exception.DRException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping(value = "download")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DownloadController {
    @Value("${path.image.examinee:C:/api/smps}") String pathRoot;
    //@Value("${path.smps:/Users/Jeremy/Humane/api/smps}") String pathRoot;
    private final StatusMapper statusMapper;
    private final DataMapper dataMapper;
    private final DataService dataService;

    @RequestMapping(value = "item.xlsx", method = RequestMethod.GET)
    public ResponseEntity item() {
        return JasperReportsExportHelper.toResponseEntity(
                "jrxml/upload-item.jrxml",
                "xlsx",
                null
        );
    }

    @RequestMapping(value = "hall.xlsx", method = RequestMethod.GET)
    public ResponseEntity hall() {
        return JasperReportsExportHelper.toResponseEntity(
                "jrxml/upload-hall.jrxml",
                "xlsx",
                null
        );
    }

    @RequestMapping(value = "examinee.xlsx", method = RequestMethod.GET)
    public ResponseEntity examinee() {
        return JasperReportsExportHelper.toResponseEntity(
                "jrxml/upload-examinee.jrxml",
                "xlsx",
                null
        );
    }

    @RequestMapping(value = "devi.xlsx", method = RequestMethod.GET)
    public ResponseEntity devi() {
        return JasperReportsExportHelper.toResponseEntity(
                "jrxml/upload-devi.jrxml",
                "xlsx",
                null
        );
    }

    @RequestMapping(value = "allData.zip", method = RequestMethod.GET)
    public ResponseEntity allData(BasicDto basicDto) throws IOException, ZipException, DRException {
        Pageable pageable = new PageRequest(0, Integer.MAX_VALUE);

        StatusDto statusDto = new StatusDto();
        statusDto.setAdmissionNm(basicDto.getAdmissionNm());
        statusDto.setTypeNm(basicDto.getTypeNm());
        statusDto.setExamDate(basicDto.getExamDate());
        statusDto.setDeptNm(basicDto.getDeptNm());
        statusDto.setMajorNm(basicDto.getMajorNm());

        ScoreDto scoreDto = new ScoreDto();
        scoreDto.setAdmissionNm(basicDto.getAdmissionNm());
        scoreDto.setTypeNm(basicDto.getTypeNm());
        scoreDto.setExamDate(basicDto.getExamDate());
        scoreDto.setDeptNm(basicDto.getDeptNm());
        scoreDto.setMajorNm(basicDto.getMajorNm());

        ExamineeDto examineeDto = new ExamineeDto();
        examineeDto.setAdmissionNm(basicDto.getAdmissionNm());
        examineeDto.setTypeNm(basicDto.getTypeNm());
        examineeDto.setExamDate(basicDto.getExamDate());
        examineeDto.setDeptNm(basicDto.getDeptNm());
        examineeDto.setMajorNm(basicDto.getMajorNm());

        // 압축파일 생성
        String currentTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        File file = new File(currentTime + "_allData.zip");
        ZipFile zipFile = new ZipFile(file);
        zipFile.setFileNameCharset("EUC-KR");

        try {
            // entry 생성
            File fileDept = JasperReportsExportHelper.toXlsxFile("jrxml/status-dept.jrxml", statusMapper.dept(statusDto, pageable).getContent());
            zipFile.addFile(fileDept);
            fileDept.delete();

            File fileMajor = JasperReportsExportHelper.toXlsxFile("jrxml/status-major.jrxml", statusMapper.major(statusDto, pageable).getContent());
            zipFile.addFile(fileMajor);
            fileMajor.delete();

            //1. xlsx 파일 생성
            File fileHall = JasperReportsExportHelper.toXlsxFile("jrxml/status-hall.jrxml", statusMapper.hall(statusDto, pageable).getContent());
            zipFile.addFile(fileHall);
            fileHall.delete();

            // 1. xlsx 파일 생성
            File fileGroup = JasperReportsExportHelper.toXlsxFile("jrxml/status-group.jrxml", statusMapper.group(statusDto, pageable).getContent());
            zipFile.addFile(fileGroup);
            fileGroup.delete();

        /*File fileExamineeReport = JasperReportsExportHelper.toXlsxFile("수험생별 종합", dataService.getExamineeReport(), dataMapper.examinee(new ExamineeDto(), pageable).getContent());
        zipFile.addFile(fileExamineeReport);
        fileExamineeReport.delete();*/

            File fileVirtNoReport = JasperReportsExportHelper.toXlsxFile("가번호 배정 현황", dataService.getVirtNoReport(), dataMapper.examinee(examineeDto, pageable).getContent());
            zipFile.addFile(fileVirtNoReport);
            fileVirtNoReport.delete();

            File fileScorerHReport = JasperReportsExportHelper.toXlsxFile("채점자별 상세(가로)", dataService.getScorerHReport(), dataService.getScorerHData(scoreDto, pageable).getContent());
            zipFile.addFile(fileScorerHReport);
            fileScorerHReport.delete();

            File fileScorerReport = JasperReportsExportHelper.toXlsxFile("채점자별 상세(세로)", dataService.getScorerReport(), dataMapper.scorer(scoreDto, pageable).getContent());
            zipFile.addFile(fileScorerReport);
            fileScorerReport.delete();

            File fileAttendanceReport = JasperReportsExportHelper.toXlsxFile("출결현황 리스트", dataService.attendanceReport(), dataMapper.attendance(examineeDto, pageable).getContent());
            zipFile.addFile(fileAttendanceReport);
            fileAttendanceReport.delete();
/*
        File fileScoreUploadReport = JasperReportsExportHelper.toXlsxFile("글로벌인재_성적업로드양식", dataService.getScoreUploadReport(), dataMapper.scoreUpload(new ScoreUploadDto(), pageable).getContent());
        zipFile.addFile(fileScoreUploadReport);
        fileScorerReport.delete();

        File fileFailList = JasperReportsExportHelper.toXlsxFile("jrxml/data-failList.jrxml", dataMapper.failList(new ExamineeDto(), pageable).getContent());
        zipFile.addFile(fileFailList);
        fileFailList.delete();

        File fileLawScorerUploadReport = JasperReportsExportHelper.toXlsxFile("법학서류평가_성적업로드양식", dataService.getScoreUploadReport(), dataMapper.lawScoreUpload(new ScoreUploadDto(), pageable).getContent());
        zipFile.addFile(fileLawScorerUploadReport);
        fileLawScorerUploadReport.delete();

        File fileMedScorerUploadReport = JasperReportsExportHelper.toXlsxFile("의대서류평가_성적업로드양식", dataService.getScoreUploadReport(), dataMapper.medScoreUpload(new ScoreUploadDto(), pageable).getContent());
        zipFile.addFile(fileMedScorerUploadReport);
        fileMedScorerUploadReport.delete();

        File fileDrawReport = JasperReportsExportHelper.toXlsxFile("동점자 현황", dataService.getDrawReport(), dataService.getScorerHData(new ScoreDto(), pageable).getContent());
        zipFile.addFile(fileDrawReport);
        fileDrawReport.delete();
*/
            // 나머지 가져오기
            // 0. 폴더위치 지정
            String jpgPath = pathRoot + "/jpg";
            // 1. 사진 폴더 생성
            File jpgFolder = new File(jpgPath);

            if (jpgFolder.exists()) {
                // 1.1 사진 가져옴
                File[] jpgList = jpgFolder.listFiles();
                if (jpgList != null) {
                    // 1.2 사진 저장
                    for (File f : jpgList) {
                        if (f.isFile())
                            zipFile.addFile("수험생서명", f);
                    }
                }
            } else jpgFolder.mkdirs();

            String pdfPath = pathRoot + "/pdf";
            // 2. pdf 폴더 생성
            File pdfFolder = new File(pdfPath);
            // 2.1 pdf 가져옴
            File[] pdfList = pdfFolder.listFiles();

            if (pdfList != null) {
                // 2.2 pdf 저장
                if (statusDto.getDeptNm() != null) {
                    for (int i = 0; i < pdfList.length; i++) {

                        String str = pdfList[i].getName().trim();
                        String deptNm = statusDto.getDeptNm().trim();

                        if (str.contains(deptNm)) {
                            File f = new File(pdfPath + "/" + pdfList[i].getName());
                            zipFile.addFile("평가위원 평가표", f);
                        }
                    }
                } else {
                    for (File f : pdfList) {
                        if (f.isFile())
                            zipFile.addFile("평가위원 평가표", f);
                    }
                }
            }

        } catch(Exception e){
            e.printStackTrace();
        }

        byte[] ba = null;

        // 압축파일 내보내기
        try (FileInputStream fis = new FileInputStream(file)) {
            ba = IOUtils.toByteArray(fis);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            file.delete();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Set-Cookie", "fileDownload=true; path=/");
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.setContentLength(ba.length);
        headers.add("Content-Disposition", FileNameEncoder.encode("최종 산출물_평가.zip"));

        return new ResponseEntity<>(ba, headers, HttpStatus.OK);
    }

    // 중간관리자 데이터
    @RequestMapping(value = "manager.zip", method = RequestMethod.GET)
    public ResponseEntity<byte[]> manager() throws ZipException {
        File file = new File("manager.zip");

        ZipFile zipFile = new ZipFile(file);
        zipFile.setFileNameCharset("EUC-KR");

        String path = pathRoot + "/smpsMgr";

        File managerFolder = new File(path);
        File[] virtNoList = managerFolder.listFiles();

        for (File f : virtNoList) {
            if (f.isFile())
                zipFile.addFile(f);
        }

        byte[] ba = FileUtils.getByteArray(zipFile.getFile());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Set-Cookie", "fileDownload=true; path=/");
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.setContentLength(ba.length);
        headers.add("Content-Disposition", FileNameEncoder.encode("중간관리자 데이터.zip"));

        return new ResponseEntity<>(ba, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "scorer.zip", method = RequestMethod.GET)
    public ResponseEntity<byte[]> scorer() throws ZipException {
        File file = new File("scorer.zip");

        ZipFile zipFile = new ZipFile(file);
        zipFile.setFileNameCharset("EUC-KR");

        String jpgPath = pathRoot + "/jpg";
        String pdfPath = pathRoot + "/pdf";

        File jpgFolder = new File(jpgPath);
        File[] jpgList = jpgFolder.listFiles();

        for (File f : jpgList) {
            if (f.isFile())
                zipFile.addFile("jpg", f);
        }

        File pdfFolder = new File(pdfPath);
        File[] pdfList = pdfFolder.listFiles();

        for (File f : pdfList) {
            if (f.isFile())
                zipFile.addFile("pdf", f);
        }

        //
        byte[] ba = FileUtils.getByteArray(zipFile.getFile());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Set-Cookie", "fileDownload=true; path=/");
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.setContentLength(ba.length);
        headers.add("Content-Disposition", FileNameEncoder.encode("평가위원 데이터.zip"));

        return new ResponseEntity<>(ba, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "signData.zip", method = RequestMethod.GET)
    public ResponseEntity signData() throws IOException, ZipException, DRException {

        // 압축파일 생성
        String currentTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        File file = new File(currentTime + "_allData.zip");
        ZipFile zipFile = new ZipFile(file);
        zipFile.setFileNameCharset("EUC-KR");

        File path = new File(pathRoot, "jpg");
        if (!path.exists()) path.mkdirs();

        File[] files = path.listFiles((dir, name) -> name.endsWith(".jpg"));
        if (files != null && files.length > 0) {
            for (File f : files) {
                if (f.isFile())
                    zipFile.addFile(f);
            }
        }

        byte[] ba = file.exists() ? FileUtils.getByteArray(file) : null;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Set-Cookie", "fileDownload=true; path=/");
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.setContentLength(ba == null ? 0 : ba.length);
        headers.add("Content-Disposition", FileNameEncoder.encode("수험생서명_에리카체육.zip"));

        return new ResponseEntity<>(ba, headers, HttpStatus.OK);
    }
}