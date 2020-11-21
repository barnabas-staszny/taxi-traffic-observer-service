package com.taxi.traffic.observer.aggregator;

import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.taxi.traffic.observer.aggregator.business.AggregatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static com.github.kagkarlsson.scheduler.task.schedule.Schedules.fixedDelay;


/*
    Configures a scheduled jobs to run only on 1 single node in a cluster.
 */
@Slf4j
@Configuration
public class AggregatorSchedulerConfiguration {

    @Autowired
    private AggregatorService aggregatorService;


    @Bean
    Task<Void> recurringAggregatorTask() {
        return Tasks
                .recurring("recurring-aggregator-task", fixedDelay(Duration.ofMinutes(1)))
                .execute((instance, ctx) -> {
                    log.debug("Running recurring-aggregator-task. Instance: {}, ctx: {}", instance, ctx);
                    aggregatorService.executeAggregationCycle();
                });
    }

}
