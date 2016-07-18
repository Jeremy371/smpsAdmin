package com.humane.smps.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.humane.util.jackson.TimeSerializer;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CheckScorerDto {
    private String admissionNm;
    private String typeNm;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date examDate;

    @DateTimeFormat(pattern = "HH:mm:ss")
    @JsonSerialize(using = TimeSerializer.class)
    private Date examTime;

    private String deptNm;
    private String majorNm;
    private String examNm;
    private String headNm;
    private String bldgNm;
    private String hallNm;

    private String examineeCd;
    private String virtNo;

    private String scorerNm;
    private Long scorerCnt;
    private Long scoredCnt;

    private Boolean isVirtNo;
}
