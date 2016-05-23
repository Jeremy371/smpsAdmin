package com.humane.admin.smps.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.awt.image.BufferedImage;
import java.util.Date;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExamineeDto {
    private String admissionNm;
    private String examNm;
    private String deptNm;
    private String majorNm;
    private String headNm;
    private String bldgNm;
    private String hallNm;

    private String examineeCd;
    private String examineeNm;
    private String virtNo;
    private Long scorerCnt;

    private String avgScore01;
    private String avgScore02;
    private String avgScore03;
    private String avgScore04;
    private String avgScore05;
    private String avgScore06;
    private String avgScore07;
    private String avgScore08;
    private String avgScore09;
    private String avgScore10;

    private String totalScore;
    private Boolean isAttend;
    private Boolean isCancel;

    private Long itemCnt;
    private Long scoredCnt;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date examDate;

    @DateTimeFormat(pattern = "HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "Asia/Seoul")
    private Date examTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Date regDttm;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Date cancelDttm;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Date scoreDttm;

    private BufferedImage examineeImage;
    private String fromExamineeCd;
    private String toExamineeCd;
}