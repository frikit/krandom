/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring.slice;

import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

/**
 * Application service that only starts with full auto-configuration, which provides the
 * {@link TaskExecutor}; inside the krandom slice it must not be scanned.
 */
@Service
public class ReportService {

    private final TaskExecutor executor;

    public ReportService(TaskExecutor executor) {
        this.executor = executor;
    }

    public TaskExecutor executor() {
        return executor;
    }
}
