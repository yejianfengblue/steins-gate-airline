package com.yejianfengblue.sga.fltsch.flt;

import com.yejianfengblue.sga.fltsch.constant.ServiceType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.ArrayList;

@ConditionalOnProperty(name = "app.flt.populator.enabled", havingValue = "true")
@Component
@Slf4j
@RequiredArgsConstructor
public class FltPopulator implements CommandLineRunner {

    @NonNull
    private final FltRepository fltRepository;

    @Override
    public void run(String... args) {

        setupSecurityContext();

        if (fltRepository.count() == 0) {

            LocalDate fltDateStart = Year.now().atMonth(1).atDay(1);
            LocalDate fltDateEnd = Year.now().atMonth(12).atDay(31);

            log.info("Flg data doesn't exist. Populate initial Flt with fltDate from {} to {}",
                    fltDateStart, fltDateEnd);

            fltDateStart.datesUntil(fltDateEnd).forEach(date -> {

                // SG520/ D1234567 / HKG -> NRT
                {
                    Flt sg520 = new Flt(null, "SG", "520", ServiceType.PAX,
                            date, date.getDayOfWeek().getValue(),
                            new ArrayList<>(),
                            null, null, null, null);
                    sg520.addFltLeg(
                            // SG520 / leg 1 / HKG 1000 -> NRT 1600
                            new FltLeg(date, date.getDayOfWeek().getValue(),
                                    "HKG", "NRT", 1,
                                    LocalDateTime.of(date, LocalTime.of(10, 00)),
                                    LocalDateTime.of(date, LocalTime.of(16, 00)),
                                    null, null, null, null,
                                    480, 540,
                                    "B-LAD", "333"));
                    fltRepository.save(sg520);
                }

                // SG521 / D1234567 / NRT -> HKG
                {
                    Flt sg521 = new Flt(null, "SG", "521", ServiceType.PAX,
                            date, date.getDayOfWeek().getValue(),
                            new ArrayList<>(),
                            null, null, null, null);
                    sg521.addFltLeg(
                            // SG101 / leg 1 / NRT 1700 -> HKG 2100
                            new FltLeg(date, date.getDayOfWeek().getValue(),
                                    "NRT", "HKG", 1,
                                    LocalDateTime.of(date, LocalTime.of(17, 00)),
                                    LocalDateTime.of(date, LocalTime.of(21, 00)),
                                    null, null, null, null,
                                    540, 480,
                                    "B-LAD", "333"));
                    fltRepository.save(sg521);
                }

                // SG296 / D246 / HKG -> ANC -> LAX -> MEX -> GDL
                if (date.getDayOfWeek().equals(DayOfWeek.TUESDAY) ||
                        date.getDayOfWeek().equals(DayOfWeek.THURSDAY) ||
                        date.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {

                    Flt sg296 = new Flt(null, "SG", "296", ServiceType.FRTR,
                            date, date.getDayOfWeek().getValue(),
                            new ArrayList<>(),
                            null, null, null, null);
                    sg296.addFltLeg(
                            // SG296 / leg 1 / HKG 2100 -> ANC 1500
                            new FltLeg(date, date.getDayOfWeek().getValue(),
                                    "HKG", "ANC", 1,
                                    LocalDateTime.of(date, LocalTime.of(21, 00)),
                                    LocalDateTime.of(date, LocalTime.of(15, 00)),
                                    null, null, null, null,
                                    480, -480,
                                    "B-LJA", "74N"));
                    sg296.addFltLeg(
                            // SG296 / leg 2 / ANC 1600 -> LAX 2200
                            new FltLeg(date, date.getDayOfWeek().getValue(),
                                    "ANC", "LAX", 2,
                                    LocalDateTime.of(date, LocalTime.of(16, 00)),
                                    LocalDateTime.of(date, LocalTime.of(22, 00)),
                                    null, null, null, null,
                                    -480, -420,
                                    "B-LJA", "74N"));
                    sg296.addFltLeg(
                            // SG296 / leg 3 / LAX 0030+1 -> MEX 0630+1
                            new FltLeg(date.plusDays(1), date.plusDays(1).getDayOfWeek().getValue(),
                                    "LAX", "MEX", 3,
                                    LocalDateTime.of(date.plusDays(1), LocalTime.of(00, 30)),
                                    LocalDateTime.of(date.plusDays(1), LocalTime.of(06, 30)),
                                    null, null, null, null,
                                    -420, -300,
                                    "B-LJA", "74N"));
                    sg296.addFltLeg(
                            // SG296 / leg 4 / MEX 0800+1 -> GDL 0900+1
                            new FltLeg(date.plusDays(1), date.plusDays(1).getDayOfWeek().getValue(),
                                    "MEX", "GDL", 4,
                                    LocalDateTime.of(date.plusDays(1), LocalTime.of(8, 00)),
                                    LocalDateTime.of(date.plusDays(1), LocalTime.of(9, 00)),
                                    null, null, null, null,
                                    -300, -300,
                                    "B-LJA", "74N"));

                }
            });
        } else {
            log.info("Flg data exists. Skip populating initial data");
        }
    }

    private void setupSecurityContext() {

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = new TestingAuthenticationToken("robot", "robot", "ROLE_flt-sch-user");
        context.setAuthentication(authentication);
    }
}
