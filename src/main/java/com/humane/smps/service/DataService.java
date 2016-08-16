package com.humane.smps.service;

import com.humane.smps.dto.ScoreDto;
import com.humane.smps.mapper.DataMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.VerticalTextAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.awt.Color.LIGHT_GRAY;
import static net.sf.dynamicreports.report.builder.DynamicReports.*;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DataService {
    private StyleBuilder boldStyle = DynamicReports.stl.style().bold();
    private StyleBuilder boldCenteredStyle = DynamicReports.stl.style(boldStyle);

    private StyleBuilder columnTitleStyle = DynamicReports.stl.style(boldCenteredStyle)
            .setTextAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.MIDDLE)
            .setPadding(2);

    private StyleBuilder columnHeaderStyle = DynamicReports.stl.style()
            .setTextAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.MIDDLE)
            .setBorder(DynamicReports.stl.penThin()).setBackgroundColor(LIGHT_GRAY);

    private StyleBuilder columnStyle = DynamicReports.stl.style()
            .setBorder(DynamicReports.stl.penThin())
            .setTextAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.MIDDLE);
    private final DataMapper mapper;

    public Object getExamineeModel() {
        // 기본 생성
        List<ColModel> colModels = new ArrayList<>();
        colModels.add(new ColModel("admissionNm", "전형"));
        colModels.add(new ColModel("typeNm", "계열"));
        colModels.add(new ColModel("examDate", "시험일자"));
        colModels.add(new ColModel("examTime", "시험시간"));
        colModels.add(new ColModel("deptNm", "모집단위"));
        colModels.add(new ColModel("majorNm", "전공"));
        colModels.add(new ColModel("headNm", "고사본부"));
        colModels.add(new ColModel("bldgNm", "고사건물"));
        colModels.add(new ColModel("hallNm", "고사실"));
        colModels.add(new ColModel("virtNo", "가번호"));

        long itemCnt = mapper.getItemCnt();
        for (int i = 1; i <= itemCnt; i++) {
            colModels.add(new ColModel("avgScore" + (i < 10 ? "0" + i : i), "항목" + i + "평균"));
            colModels.add(new ColModel("totScore" + (i < 10 ? "0" + i : i), "항목" + i + "합계"));
        }
        colModels.add(new ColModel("scorerCnt", "평가위원수"));
        colModels.add(new ColModel("isAttend", "응시여부"));
        return colModels;
    }

    public List<ColModel> getScorerModel() {
        // 기본 생성
        List<ColModel> colModels = new ArrayList<>();
        colModels.add(new ColModel("admissionNm", "전형"));
        colModels.add(new ColModel("typeNm", "계열"));
        colModels.add(new ColModel("examDate", "시험일자"));
        colModels.add(new ColModel("examTime", "시험시간"));
        colModels.add(new ColModel("deptNm", "모집단위"));
        colModels.add(new ColModel("majorNm", "전공"));
        colModels.add(new ColModel("headNm", "고사본부"));
        colModels.add(new ColModel("bldgNm", "고사건물"));
        colModels.add(new ColModel("hallNm", "고사실"));
        colModels.add(new ColModel("examineeCd", "수험번호"));
        colModels.add(new ColModel("examineeNm", "수험생명"));
        colModels.add(new ColModel("virtNo", "가번호"));
        colModels.add(new ColModel("groupNm", "조"));
        colModels.add(new ColModel("scorerNm", "평가위원"));

        long itemCnt = mapper.getItemCnt();

        for (int i = 1; i <= itemCnt; i++)
            colModels.add(new ColModel("score" + (i < 10 ? "0" + i : i), "항목" + i));

        colModels.add(new ColModel("totalScore", "총점"));
        colModels.add(new ColModel("memo", "메모"));
        colModels.add(new ColModel("isPhoto", "사진"));
        colModels.add(new ColModel("scoreDttm", "채점시간"));

        return colModels;
    }

    public JasperReportBuilder getExamineeReport() {
        JasperReportBuilder report = report()
                .title(cmp.text("수험생별 종합").setStyle(columnTitleStyle))
                .columns(
                        col.reportRowNumberColumn("번호").setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("전형", "admissionNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("계열", "typeNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("시험일자", "examDate", type.dateType()).setPattern("yyyy-MM-dd").setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(8),
                        col.column("시험시간", "examTime", type.dateType()).setPattern("HH:mm:ss").setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(8),
                        col.column("모집단위", "deptNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("전공", "majorNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("고사본부", "headNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("고사건물", "bldgNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("고사실", "hallNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("가번호", "virtNo", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7)
                )
                .setPageMargin(DynamicReports.margin(0))
                .setIgnorePageWidth(true)
                .setIgnorePagination(true);

        long itemCnt = mapper.getItemCnt();

        for (int i = 1; i <= itemCnt; i++) {
            report.addColumn(col.column("항목" + i + "합계", "totScore" + (i < 10 ? "0" + i : i), type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7));
            report.addColumn(col.column("항목" + i + "평균", "avgScore" + (i < 10 ? "0" + i : i), type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7));
        }
        report.addColumn(col.column("평가위원수", "scorerCnt", type.longType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7));
        report.addColumn(col.column("응시여부", "isAttend", type.booleanType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7));

        return report;

    }

    public JasperReportBuilder getVirtNoReport() {
        JasperReportBuilder report = report()
                .title(cmp.text("가번호 배정 현황").setStyle(columnTitleStyle))
                .columns(
                        col.reportRowNumberColumn("번호").setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("가번호", "virtNo", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("조", "groupNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("수험번호", "examineeCd", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("수험생명", "examineeNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("전형", "admissionNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("계열", "typeNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("시험일자", "examDate", type.dateType()).setPattern("yyyy-MM-dd").setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(8),
                        col.column("시험시간", "examTime", type.dateType()).setPattern("HH:mm:ss").setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(8),
                        col.column("모집단위", "deptNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("전공", "majorNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("고사본부", "headNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(9),
                        col.column("고사건물", "bldgNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("고사실", "hallNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7)
                )
                .setPageMargin(DynamicReports.margin(0))
                .setIgnorePageWidth(true)
                .setIgnorePagination(true);
        return report;
    }

    public JasperReportBuilder getScorerHReport() {
        JasperReportBuilder report = report()
                .title(cmp.text("채점자별 상세(가로)").setStyle(columnTitleStyle))
                .columns(
                        col.reportRowNumberColumn("번호").setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("전형", "admissionNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("계열", "typeNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("시험일자", "examDate", type.dateType()).setPattern("yyyy-MM-dd").setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(8),
                        col.column("시험시간", "examTime", type.dateType()).setPattern("HH:mm:ss").setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(8),
                        col.column("모집단위", "deptNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("전공", "majorNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("고사본부", "headNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("고사건물", "bldgNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("고사실", "hallNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("수험번호", "examineeCd", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("수험생명", "examineeNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("가번호", "virtNo", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("조", "groupNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7)
                )
                .setPageMargin(DynamicReports.margin(0))
                .setIgnorePageWidth(true)
                .setIgnorePagination(true);

        long scorerCnt = mapper.getScorerCnt();
        long itemCnt = mapper.getItemCnt();

        for (int i = 1; i <= scorerCnt; i++) {
            report.addColumn(col.column("평가위원", "scorerNm" + i, type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7));
            for (int j = 1; j <= itemCnt; j++)
                report.addColumn(col.column("항목" + j, "score" + i + "S" + j, type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7));
            report.addColumn(col.column("총점" + i, "totalScore" + i, type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7));
            report.addColumn(col.column("채점시간", "scoreDttm" + i, type.stringType()).setPattern("yyyy-MM-dd").setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(12));
        }
        return report;
    }

    public JasperReportBuilder getScorerReport() {
        JasperReportBuilder report = report()
                .title(cmp.text("채점자별 상세(세로)").setStyle(columnTitleStyle))
                .columns(
                        col.reportRowNumberColumn("번호").setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("전형", "admissionNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("계열", "typeNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("시험일자", "examDate", type.dateType()).setPattern("yyyy-MM-dd").setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(8),
                        col.column("시험시간", "examTime", type.dateType()).setPattern("HH:mm:ss").setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(8),
                        col.column("모집단위", "deptNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("전공", "majorNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("고사본부", "headNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("고사건물", "bldgNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("고사실", "hallNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("수험번호", "examineeCd", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("수험생명", "examineeNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("가번호", "virtNo", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("조", "groupNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7),
                        col.column("평가위원", "scorerNm", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7)
                )
                .setPageMargin(DynamicReports.margin(0))
                .setIgnorePageWidth(true)
                .setIgnorePagination(true);

        long itemCnt = mapper.getItemCnt();
        for (int i = 1; i <= itemCnt; i++)
            report.addColumn(col.column("항목" + i, "score" + (i < 10 ? "0" + i : i), type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7));

        report.addColumn(col.column("총점", "totalScore", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7));
        report.addColumn(col.column("메모", "memo", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7));
        report.addColumn(col.column("사진", "isPhoto", type.stringType()).setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(7));
        report.addColumn(col.column("채점시간", "scoreDttm", type.dateType()).setPattern("yyyy-MM-dd HH:mm:ss").setTitleStyle(columnHeaderStyle).setStyle(columnStyle).setFixedColumns(12));
        return report;

    }

    @Data
    private class ColModel {
        private String name;
        private String label;
        private boolean sortable = true;

        public ColModel(String name, String label) {
            this.name = name;
            this.label = label;
        }

        public ColModel(String name, String label, boolean sortable) {
            this(name, label);
            this.sortable = sortable;
        }
    }

    public Page<Map<String, Object>> getScorerHData(ScoreDto param, Pageable pageable) {
        Page<Map<String, Object>> page = mapper.examMap(param, pageable);
        page.forEach(map -> {
            String examCd = map.get("examCd") == null ? null : map.get("examCd").toString();
            String virtNo = map.get("virtNo") == null ? null : map.get("virtNo").toString();

            if (examCd != null && virtNo != null) {
                List<Map<String, Object>> scoreList = mapper.scorerH(map);
                for (int i = 1; i <= scoreList.size(); i++) {
                    Map<String, Object> score = scoreList.get(i - 1);
                    log.debug("score: {}", score);
                    if (score != null) {
                        map.put("scorerNm" + i, score.get("scorerNm"));
                        map.put("score" + i + "S1", score.get("score01"));
                        map.put("score" + i + "S2", score.get("score02"));
                        map.put("score" + i + "S3", score.get("score03"));
                        map.put("score" + i + "S4", score.get("score04"));
                        map.put("score" + i + "S5", score.get("score05"));
                        map.put("score" + i + "S6", score.get("score06"));
                        map.put("score" + i + "S7", score.get("score07"));
                        map.put("score" + i + "S8", score.get("score08"));
                        map.put("score" + i + "S9", score.get("score09"));
                        map.put("score" + i + "S10", score.get("score10"));
                        map.put("totalScore" + i, score.get("totalScore"));
                        map.put("scoreDttm" + i, score.get("scoreDttm"));
                    }
                    log.debug("map:{}", map);
                }
            }
        });

        return page;
    }

    public List<ColModel> getScorerHModel() {
        // 기본 생성
        List<ColModel> colModels = new ArrayList<>();
        colModels.add(new ColModel("admissionNm", "전형"));
        colModels.add(new ColModel("typeNm", "계열"));
        colModels.add(new ColModel("examDate", "시험일자"));
        colModels.add(new ColModel("examTime", "시험시간"));
        colModels.add(new ColModel("deptNm", "모집단위"));
        colModels.add(new ColModel("majorNm", "전공"));
        colModels.add(new ColModel("headNm", "고사본부"));
        colModels.add(new ColModel("bldgNm", "고사건물"));
        colModels.add(new ColModel("hallNm", "고사실"));
        colModels.add(new ColModel("examineeCd", "수험번호"));
        colModels.add(new ColModel("examineeNm", "수험생명"));
        colModels.add(new ColModel("virtNo", "가번호"));
        colModels.add(new ColModel("groupNm", "조"));

        long scorerCnt = mapper.getScorerCnt(); // 채점자수
        long itemCnt = mapper.getItemCnt(); // 항목수

        for (int i = 1; i <= scorerCnt; i++) {
            colModels.add(new ColModel("scorernm" + i, "평가위원" + i, false));
            for (int j = 1; j <= itemCnt; j++) colModels.add(new ColModel("score" + i + "s" + j, "항목" + j, false));

            colModels.add(new ColModel("totalscore" + i, "총점" + i, false));
            colModels.add(new ColModel("scoredttm" + i, "채점시간" + i, false));
        }
        return colModels;
    }
}