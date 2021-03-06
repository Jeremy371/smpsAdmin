package com.humane.smps.controller.admin;

import com.humane.smps.dto.CheckDto;
import com.humane.smps.dto.ScoreDto;
import com.humane.smps.mapper.CheckMapper;
import com.humane.smps.service.CheckService;
import com.humane.util.jasperreports.JasperReportsExportHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "check", method = RequestMethod.GET)
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CheckController {
    private static final String JSON = "json";
    private static final String COLMODEL = "colmodel";
    private final CheckService checkService;
    private final CheckMapper checkMapper;

    @RequestMapping(value = "item.{format:json|pdf|xls|xlsx}")
    public ResponseEntity item(@PathVariable String format, CheckDto param, Pageable pageable) {
        switch (format) {
            case JSON:
                return ResponseEntity.ok(checkMapper.item(param, pageable));
            default:
                return JasperReportsExportHelper.toResponseEntity(
                        "jrxml/check-item.jrxml"
                        , format
                        , checkMapper.item(param, new PageRequest(0, Integer.MAX_VALUE, pageable.getSort())).getContent());
        }
    }

    @RequestMapping(value = "scorer.{format:json|pdf|xls|xlsx}")
    public ResponseEntity scorer(@PathVariable String format, CheckDto param, Pageable pageable) {
        switch (format) {
            case JSON:
                return ResponseEntity.ok(checkMapper.scorer(param, pageable));
            default:
                return JasperReportsExportHelper.toResponseEntity(
                        "jrxml/check-scorer.jrxml"
                        , format
                        , checkMapper.scorer(param, new PageRequest(0, Integer.MAX_VALUE, pageable.getSort())).getContent());
        }
    }

    @RequestMapping(value = "scoredCnt.{format:colmodel|json|xls|xlsx}")
    public ResponseEntity scoredCnt(@PathVariable String format, ScoreDto param, Pageable pageable) throws DRException, JRException {
        switch (format) {
            case COLMODEL:
                return ResponseEntity.ok(checkService.getScoredCntModel());
            case JSON:
                return ResponseEntity.ok(checkService.getScoredCntData(param, pageable));
            default:
                JasperReportBuilder report = checkService.getScoredCntReport();
                report.setDataSource(checkService.getScoredCntData(param, new PageRequest(0, Integer.MAX_VALUE, pageable.getSort())).getContent());

                JasperPrint jasperPrint = report.toJasperPrint();
                jasperPrint.setName("채점인원 수 불일치 리스트");

                return JasperReportsExportHelper.toResponseEntity(jasperPrint, format);
        }
    }

    @RequestMapping(value = "scoredF.{format:colmodel|json|xls|xlsx}")
    public ResponseEntity scoredF(@PathVariable String format, ScoreDto param, Pageable pageable) throws DRException, JRException {

        // '결시'를 어떤 값으로 할 것 인지 사전에 설정 - 예정

        switch (format) {
            case COLMODEL:
                return ResponseEntity.ok(checkService.getScoredFModel());
            case JSON:
                return ResponseEntity.ok(checkService.getScoredFData(param, pageable));
            default:
                JasperReportBuilder report = checkService.getScoredFReport();
                report.setDataSource(checkService.getScoredFData(param, new PageRequest(0, Integer.MAX_VALUE, pageable.getSort())).getContent());

                JasperPrint jasperPrint = report.toJasperPrint();
                jasperPrint.setName("결시 항목 불일치 리스트");

                return JasperReportsExportHelper.toResponseEntity(jasperPrint, format);
        }
    }
}