package com.humane.smps.controller.admin;

import com.humane.smps.dto.ExamInfoDto;
import com.humane.smps.dto.StatusDto;
import com.humane.smps.mapper.ModelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "model", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ModelController {
    private final ModelMapper mapper;

    /**
     * 툴바 데이터를 전송
     */
    @RequestMapping(value = "toolbar.json")
    public ResponseEntity toolbar(StatusDto statusDto) {
        return ResponseEntity.ok(mapper.toolbar(statusDto));
    }

    @RequestMapping(value = "examInfoToolbar.json")
    public ResponseEntity examInfoToolbar(ExamInfoDto examInfoDto) {
        List<ExamInfoDto> list = mapper.examToolbar(examInfoDto);
        log.debug("{}", list);
        return ResponseEntity.ok(list);
    }
}
