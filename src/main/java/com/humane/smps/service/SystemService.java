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
import org.apache.commons.lang3.StringUtils;
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

import static com.humane.smps.model.QExamMap.examMap;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SystemService {
    private final DeviRepository deviRepository;
    private final AdmissionRepository admissionRepository;
    private final ExamRepository examRepository;
    private final HallRepository hallRepository;
    private final ExamHallRepository examHallRepository;
    private final ExamineeRepository examineeRepository;
    private final ExamMapRepository examMapRepository;
    private final ItemRepository itemRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${path.image.examinee:C:/api/image/examinee}")
    String pathExaminee;

    private final ImageService imageService;

    @Transactional
    public void resetData(boolean photo) throws IOException {
        HibernateQueryFactory queryFactory = new HibernateQueryFactory(entityManager.unwrap(Session.class));

        queryFactory.delete(QSheet.sheet).execute();
        queryFactory.delete(QScore.score).execute();
        queryFactory.delete(QScoreLog.scoreLog).execute();
        queryFactory.delete(QExamHall.examHall).execute();

        QExaminee examinee = QExaminee.examinee;

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
            } catch (Exception ignored) {
            }
        }
        scrollableResults.close();

        queryFactory.delete(QHall.hall).execute();
        queryFactory.delete(QItem.item).execute();
        queryFactory.delete(QDevi.devi1).where(QDevi.devi1.devi.isNotNull()).execute();
        queryFactory.delete(QDevi.devi1).execute();


        QExam exam = QExam.exam;

        scrollableResults = queryFactory.select(exam.admission.admissionCd)
                .distinct()
                .from(exam)
                .setFetchSize(Integer.MIN_VALUE)
                .scroll(ScrollMode.FORWARD_ONLY);

        while (scrollableResults.next()) {
            String admissionCd = scrollableResults.getString(0);
            queryFactory.delete(exam).where(exam.admission.admissionCd.eq(admissionCd));

            try {
                queryFactory.delete(QAdmission.admission).where(QAdmission.admission.admissionCd.eq(admissionCd)).execute();
            } catch (Exception ignored) {
            }
        }

        scrollableResults.close();

        // delete photo
        if (photo) {
            imageService.deleteImage(pathExaminee);
        }

    }

    @Transactional
    public void initData() {
        HibernateQueryFactory queryFactory = new HibernateQueryFactory(entityManager.unwrap(Session.class));

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
                .execute();
    }

    public void saveExamMap(ApiService apiService, DownloadWrapper wrapper) {
        for (DownloadWrapper.ExamHallWrapper examHallWrapper : wrapper.getList()) {
            String newExamCd = examHallWrapper.getExamCd();
            String newHallCd = examHallWrapper.getHallCd();

            Observable.range(0, Integer.MAX_VALUE)
                    .concatMap(page -> apiService.examMap(new QueryBuilder().add("exam.examCd", newExamCd).add("hall.hallCd", newHallCd).getMap(), page, Integer.MAX_VALUE, null))
                    .takeUntil(page -> page.last)
                    .flatMap(page -> {
                        String examCd = null;
                        String hallCd = null;
                        for (ExamMap examMap : page.content) {
                            admissionRepository.save(examMap.getExam().getAdmission());
                            examRepository.save(examMap.getExam());
                            hallRepository.save(examMap.getHall());

                            // examHall이 없으면 생성
                            if (!StringUtils.equals(examCd, examMap.getExam().getExamCd())
                                    || !StringUtils.equals(hallCd, examMap.getHall().getHallCd())) {
                                examCd = examMap.getExam().getExamCd();
                                hallCd = examMap.getHall().getHallCd();

                                ExamHall examHall = new ExamHall();
                                examHall.setExam(examMap.getExam());
                                examHall.setHall(examMap.getHall());

                                ExamHall findExamHall = examHallRepository.findOne(new BooleanBuilder()
                                        .and(QExamHall.examHall.exam.eq(examMap.getExam()))
                                        .and(QExamHall.examHall.hall.eq(examMap.getHall()))
                                );

                                if (findExamHall != null) examHall.set_id(findExamHall.get_id());
                                examHallRepository.save(examHall);
                            }

                            examineeRepository.save(examMap.getExaminee());

                            ExamMap findExamMap = examMapRepository.findOne(new BooleanBuilder()
                                    .and(QExamMap.examMap.examinee.eq(examMap.getExaminee()))
                                    .and(QExamMap.examMap.exam.eq(examMap.getExam()))
                            );

                            if (findExamMap != null) examMap.set_id(findExamMap.get_id());

                            examMapRepository.save(examMap);
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

        List<String> deviList = new ArrayList<>();

        examCds.forEach(examCd -> {
            Observable.range(0, Integer.MAX_VALUE)
                    .concatMap(page -> apiService.item(new QueryBuilder().add("exam.examCd", examCd).getMap(), page, Integer.MAX_VALUE, null))
                    .takeUntil(page -> page.last)
                    .map(page -> {
                        page.content.forEach(item -> {
                            deviRepository.save(item.getDevi());
                            examRepository.save(item.getExam());

                            Item findItem = itemRepository.findOne(new BooleanBuilder()
                                    .and(QItem.item.exam.eq(item.getExam()))
                                    .and(QItem.item.itemNo.eq(item.getItemNo()))
                            );

                            if (findItem != null) item.set_id(findItem.get_id());

                            itemRepository.save(item);

                            if (!deviList.contains(item.getDevi().getDeviCd()))
                                deviList.add(item.getDevi().getDeviCd());
                        });
                        return null;
                    })
                    .toBlocking().first();
        });

        deviList.forEach(deviCd -> {
            Observable.range(0, Integer.MAX_VALUE)
                    .concatMap(page -> apiService.devi(new QueryBuilder().add("devi.deviCd", deviCd).getMap(), page, Integer.MAX_VALUE, null))
                    .takeUntil(page -> page.last)
                    .map(page -> {
                        deviRepository.save(page.content);
                        return null;
                    })
                    .toBlocking().first();
        });
    }
}