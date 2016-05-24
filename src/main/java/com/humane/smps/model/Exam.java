package com.humane.smps.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Table(name = "exam")
public class Exam {
    @Id private String examCd; // 시험코드
    private String examNm; // 시험명

    @ManyToOne @JoinColumn(name = "admissionCd", nullable = false) private Admission admission; // 전형

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @Temporal(TemporalType.DATE)
    private Date examDate; // 시험일자

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    @Temporal(TemporalType.TIME)
    private Date examTime; // 시험시간

    @Column(columnDefinition = "int default 100") private int totScore; // 총점

    private String virtNoType; // 가번호 타입. 수험번호? 답안지번호? 순차번호?
    @Column(columnDefinition = "int default 0") private int scorerCnt; // 채점자 수. 검증 및 통계에 쓰임.
    private String virtNoHead; // 가번호 헤더값.
    @Column(columnDefinition = "bit default 1") private boolean isHeadShow; // 헤더를 가번호에 포함할 여부
    @Column(columnDefinition = "int default 1") private int virtNoStart; // 가번호 시작점. 헤더값과는 상관없음.
    @Column(columnDefinition = "int default 100") private int virtNoEnd; // 가번호 종료점.
    @Column(columnDefinition = "int default 4") private int virtNoLen; // 가번호+ 헤더 or 가번호
    private String virtNoAssignType; // 가번호 할당 방식.
    @Column(columnDefinition = "bit default 1") private boolean isHorizontal; // 채점방식. 가로, 세로
    @Column(columnDefinition = "bit default 0") private boolean isMove; // 지정이동? 순차이동? => uplus 기준 문제별 채점, 학생별 채점
    @Column(columnDefinition = "bit default 0") private boolean isClosedView;  // 타 사용자가 이전 채점자의 마감데이터를 볼지 여부 결정.
    @Column(columnDefinition = "bit default 0") private boolean isPaperScan;  // 답안지 스캔 여부
    @Column(columnDefinition = "int default 0") private int paperLen;  // 답안지 자리수.
    private String keypadType;
}