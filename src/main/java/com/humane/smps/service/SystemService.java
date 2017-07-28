package com.humane.smps.service;

import com.humane.smps.dto.DownloadWrapper;
import com.humane.smps.model.*;
import com.humane.smps.repository.*;
import com.humane.util.retrofit.QueryBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.hibernate.HibernateQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rx.Observable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SystemService {
    private final AdmissionRepository admissionRepository;
    private final ExamRepository examRepository;
    private final HallRepository hallRepository;
    private final ExamineeRepository examineeRepository;
    private final ExamMapRepository examMapRepository;
    private final ItemRepository itemRepository;
    private final HallDateRepository hallDateRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${path.image.examinee:C:/api/image/examinee}") String pathExaminee;
    @Value("${path.smps.jpg:C:/api/smps/jpg}") String pathJpg;
    @Value("${path.smps.pdf:C:/api/smps/pdf}") String pathPdf;

    @Transactional
    public void resetData(boolean photo) throws IOException {
        HibernateQueryFactory queryFactory = new HibernateQueryFactory(entityManager.unwrap(Session.class));

        queryFactory.delete(QSheet.sheet).execute();
        queryFactory.delete(QScore.score).execute();
        queryFactory.delete(QScoreLog.scoreLog).execute();
        queryFactory.delete(QExamHallDate.examHallDate).execute();

        QExaminee examinee = QExaminee.examinee;
        QExamMap examMap = QExamMap.examMap;

        ScrollableResults scrollableResults = queryFactory.select(examMap.examinee.examineeCd)
                .distinct()
                .from(examMap)
                .setFetchSize(Integer.MIN_VALUE)
                .scroll(ScrollMode.FORWARD_ONLY);

        while (scrollableResults.next()) {
            String examineeCd = scrollableResults.getString(0);
            queryFactory.delete(examMap).where(examMap.examinee.examineeCd.eq(examineeCd)).execute();
            try {
                queryFactory.delete(examinee).where(examinee.examineeCd.eq(examineeCd)).execute();
                if (photo) new File(pathExaminee, examineeCd + ".jpg").delete();

            } catch (Exception ignored) {
            }
        }
        scrollableResults.close();

        try {
            queryFactory.delete(QHall.hall).execute();
        } catch (Exception ignored) {
        }
        queryFactory.delete(QItem.item).execute();

        QExam exam = QExam.exam;

        // fk_exam_cd가 존재하는 시험부터 삭제
        scrollableResults = queryFactory.select(exam.admission.admissionCd)
                .distinct()
                .from(exam)
                .where(exam.fkExam.examCd.isNotNull())
                .setFetchSize(Integer.MIN_VALUE)
                .scroll(ScrollMode.FORWARD_ONLY);

        while (scrollableResults.next()) {

            String admissionCd = scrollableResults.getString(0);
            queryFactory.delete(exam).where(
                    exam.admission.admissionCd.eq(admissionCd)
                            .and(exam.fkExam.examCd.isNotNull())
            ).execute();
        }

        scrollableResults.close();

        // 나머지 exam 삭제
        scrollableResults = queryFactory.select(exam.admission.admissionCd)
                .distinct()
                .from(exam)
                .setFetchSize(Integer.MIN_VALUE)
                .scroll(ScrollMode.FORWARD_ONLY);

        while (scrollableResults.next()) {
            String admissionCd = scrollableResults.getString(0);
            queryFactory.delete(exam)
                    .where(exam.admission.admissionCd.eq(admissionCd))
                    .execute();

            try {
                queryFactory.delete(QAdmission.admission)
                        .where(QAdmission.admission.admissionCd.eq(admissionCd))
                        .execute();
            } catch (Exception e) {
                log.error("{}", e.getMessage());
            }
        }

        scrollableResults.close();

        deleteFiles(pathJpg, pathPdf);

    }

    @Transactional
    public void initData(String examCd) throws IOException {
        HibernateQueryFactory queryFactory = new HibernateQueryFactory(entityManager.unwrap(Session.class));

        if (examCd != null) {
            queryFactory.delete(QSheet.sheet).where(QSheet.sheet.exam.examCd.eq(examCd)).execute();
            queryFactory.delete(QScoreLog.scoreLog).where(QScoreLog.scoreLog.exam.examCd.eq(examCd)).execute();
            queryFactory.delete(QScore.score).where(QScore.score.exam.examCd.eq(examCd)).execute();

            QExamMap examMap = QExamMap.examMap;

            queryFactory.update(examMap)
                    .setNull(examMap.virtNo)
                    .setNull(examMap.scanDttm)
                    .setNull(examMap.photoNm)
                    .setNull(examMap.memo)
                    .setNull(examMap.evalCd)
                    .setNull(examMap.groupNm)
                    .setNull(examMap.groupOrder)
                    .setNull(examMap.debateNm)
                    .setNull(examMap.debateOrder)
                    .where(examMap.exam.examCd.eq(examCd))
                    .execute();
        } else {
            queryFactory.delete(QSheet.sheet).execute();
            queryFactory.delete(QScoreLog.scoreLog).execute();
            queryFactory.delete(QScore.score).execute();

            QExamMap examMap = QExamMap.examMap;
            queryFactory.update(examMap)
                    .setNull(examMap.virtNo)
                    .setNull(examMap.scanDttm)
                    .setNull(examMap.photoNm)
                    .setNull(examMap.memo)
                    .setNull(examMap.evalCd)
                    .setNull(examMap.groupNm)
                    .setNull(examMap.groupOrder)
                    .setNull(examMap.debateNm)
                    .setNull(examMap.debateOrder)
                    .execute();
        }

        deleteFiles(pathJpg, pathPdf);
    }

    public void deleteFiles(String... path) throws IOException {
        for (String p : path) {
            File folder = new File(p);
            File[] files = folder.listFiles();
            if (files != null)
                for (File file : files) {
                    file.delete();
                }
        }
    }

    public void saveExamMap(ApiService apiService, DownloadWrapper wrapper) {
        for (DownloadWrapper.ExamHallWrapper examHallWrapper : wrapper.getList()) {
            String newExamCd = examHallWrapper.getExamCd();

            Observable.range(0, Integer.MAX_VALUE)
                    .concatMap(page -> apiService.examMap(new QueryBuilder().add("exam.examCd", newExamCd).getMap(), page, Integer.MAX_VALUE))
                    .takeUntil(page -> page.last)
                    .flatMap(page -> {
                        for (ExamMap examMap : page.content) {
                            admissionRepository.save(examMap.getExam().getAdmission());

                            Hall hall = examMap.getHall();
                            Exam exam = examMap.getExam();
                            Examinee examinee = examMap.getExaminee();

                            hallRepository.save(examMap.getHall());
                            examRepository.save(examMap.getExam());

                            ExamHallDate findExamHallDate = hallDateRepository.findOne(new BooleanBuilder()
                                    .and(QExamHallDate.examHallDate.exam.examCd.eq(exam.getExamCd()))
                                    .and(QExamHallDate.examHallDate.hall.hallCd.eq(hall.getHallCd()))
                                    .and(QExamHallDate.examHallDate.hallDate.eq(examMap.getHallDate()))
                            );

                            if (findExamHallDate == null) {
                                ExamHallDate examHallDate = new ExamHallDate();
                                examHallDate.setExam(exam);
                                examHallDate.setHall(hall);
                                examHallDate.setHallDate(examMap.getHallDate());
                                hallDateRepository.save(examHallDate);
                            }

                            examineeRepository.save(examinee);

                            ExamMap findExamMap = examMapRepository.findOne(new BooleanBuilder()
                                    .and(QExamMap.examMap.examinee.examineeCd.eq(examinee.getExamineeCd()))
                                    .and(QExamMap.examMap.exam.examCd.eq(exam.getExamCd()))
                                    .and(QExamMap.examMap.hall.hallCd.eq(hall.getHallCd()))
                            );

                            examMap.set_id(null); // 서버에서 받아오는 ID가 같으면 덮어씌워버릴 수 있음. ID값 삭제
                            if (findExamMap == null) examMapRepository.save(examMap);

                        }
                        return Observable.from(page.content);
                    })
                    .flatMap(examMap -> Observable.just(examMap.getExaminee().getExamineeCd() + ".jpg"))
                    .flatMap(fileName -> imageExaminee(apiService, fileName))
                    .reduce(new ArrayList<>(), (list, file) -> list)
                    .toBlocking().first();
        }
    }

    private Observable<File> imageExaminee(ApiService apiService, String fileName) {
        File path = new File(pathExaminee);

        if (!path.exists()) path.mkdirs();
        File file = new File(path, fileName);

        if (file.exists()) {
            return Observable.just(file);
        } else {
            return apiService.imageExaminee(fileName)
                    .flatMap(responseBody -> {
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            IOUtils.write(responseBody.bytes(), fos);
                            return Observable.just(file);
                        } catch (IOException e) {
                            return Observable.error(e);
                        }
                    });
        }
    }

    public void saveItem(ApiService apiService, DownloadWrapper wrapper) {
        List<String> examCds = new ArrayList<>();
        wrapper.getList().forEach(examHallWrapper -> {
            if (!examCds.contains(examHallWrapper.getExamCd()))
                examCds.add(examHallWrapper.getExamCd());
        });

        examCds.forEach(examCd -> {
            Observable.range(0, Integer.MAX_VALUE)
                    .concatMap(page -> apiService.item(new QueryBuilder().add("exam.examCd", examCd).getMap(), page, Integer.MAX_VALUE, null))
                    .takeUntil(page -> page.last)
                    .map(page -> {
                        page.content.forEach(item -> {
                            examRepository.save(item.getExam());

                            Item findItem = itemRepository.findOne(new BooleanBuilder()
                                    .and(QItem.item.exam.examCd.eq(item.getExam().getExamCd()))
                                    .and(QItem.item.itemNo.eq(item.getItemNo()))
                            );

                            if (findItem != null) item.set_id(findItem.get_id());

                            itemRepository.save(item);
                        });
                        return null;
                    })
                    .toBlocking().first();
        });
    }

    public void saveOrder(ApiService apiService, String admissionCd) {

        Observable.range(0, Integer.MAX_VALUE)
                .concatMap(page -> apiService.examMap(new QueryBuilder().add("exam.admission.admissionCd", admissionCd).getMap(), page, Integer.MAX_VALUE))
                .takeUntil(page -> page.last)
                .flatMap(page -> {

                    for (ExamMap examMap : page.content) {

                        Exam exam = examMap.getExam();
                        Examinee examinee = examMap.getExaminee();

                        // 일반면접 검사
                        ExamMap findExamMap = examMapRepository.findOne(new BooleanBuilder()
                                .and(QExamMap.examMap.examinee.examineeCd.eq(examinee.getExamineeCd()))
                                .and(QExamMap.examMap.exam.examCd.eq(exam.getExamCd()))
                        );

                        examMap.set_id(null); // 서버에서 받아오는 ID가 같으면 덮어씌워버릴 수 있음. ID값 삭제

                        if(findExamMap != null) {
                            findExamMap.setGroupNm(examMap.getGroupNm());
                            findExamMap.setGroupOrder(examMap.getGroupOrder());
                            findExamMap.setDebateNm(examMap.getDebateNm());
                            findExamMap.setDebateOrder(examMap.getDebateOrder());

                            examMapRepository.save(findExamMap);
                        }
                    }

                    return Observable.from(page.content);
                })
                .toBlocking().first();
    }
}
