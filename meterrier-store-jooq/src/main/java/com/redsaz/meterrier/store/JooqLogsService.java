/*
 * Copyright 2016 Redsaz <redsaz@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redsaz.meterrier.store;

import com.redsaz.meterrier.api.LogsService;
import com.redsaz.meterrier.api.exceptions.AppServerException;
import com.redsaz.meterrier.api.model.Log;
import com.redsaz.meterrier.api.model.Log.Status;
import static com.redsaz.meterrier.model.tables.Log.LOG;
import com.redsaz.meterrier.model.tables.records.LogRecord;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.RecordHandler;
import org.jooq.RecordMapper;
import org.jooq.SQLDialect;
import org.jooq.UpdateQuery;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores and accesses logs.
 *
 * @author Redsaz <redsaz@gmail.com>
 */
public class JooqLogsService implements LogsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JooqLogsService.class);

    private static final RecordToLogMapper R2L = new RecordToLogMapper();

    private final ConnectionPool pool;
    private final SQLDialect dialect;
    private final String logsDir;

    /**
     * Create a new LogsService backed by a data store.
     *
     * @param jdbcPool opens connections to database
     * @param sqlDialect the type of SQL database that we should speak
     * @param logsDirectory the directory containing the logs
     */
    public JooqLogsService(ConnectionPool jdbcPool, SQLDialect sqlDialect, String logsDirectory) {
        pool = jdbcPool;
        dialect = sqlDialect;
        logsDir = logsDirectory;
    }

    @Override
    public Log create(Log source) {
        if (source == null) {
            throw new NullPointerException("No log information was specified.");
        } else if (source.getStatus() == null) {
            throw new NullPointerException("Log status must not be null.");
        } else if (source.getNotes() == null) {
            throw new NullPointerException("Log notes must not be null.");
        } else if (source.getName() == null) {
            throw new NullPointerException("Log title must not be null.");
        } else if (source.getUriName() == null) {
            throw new NullPointerException("Log uriName must not be null.");
        }

        LOGGER.info("Creating entry in DB...");
        try (Connection c = pool.getConnection()) {
            DSLContext context = DSL.using(c, dialect);

            LogRecord result = context.insertInto(LOG,
                    LOG.STATUS,
                    LOG.URI_NAME,
                    LOG.NAME,
                    LOG.DATA_FILE,
                    LOG.NOTES).values(
                            source.getStatus().ordinal(),
                            source.getUriName(),
                            source.getName(),
                            source.getDataFile(),
                            source.getNotes())
                    .returning().fetchOne();
            LOGGER.info("...Created log entry in DB.");
            return R2L.map(result);
        } catch (SQLException ex) {
            throw new AppServerException("Failed to create log: " + ex.getMessage(), ex);
        }

    }

    @Override
    public OutputStream getContent(long id) {
//        try (Connection c = POOL.getConnection()) {
//            DSLContext context = DSL.using(c, dialect);
//
//            LogRecord nr = context.selectFrom(LOG).where(LOG.ID.eq(id)).fetchOne();
//            return recordToLog(nr);
//        } catch (SQLException ex) {
//            throw new AppServerException("Cannot get log_id=" + id + " because: " + ex.getMessage(), ex);
//        }
        return null;
    }

    @Override
    public Log get(long id) {
        try (Connection c = pool.getConnection()) {
            DSLContext context = DSL.using(c, dialect);
            return context.selectFrom(LOG)
                    .where(LOG.ID.eq(id))
                    .fetchOne(R2L);
        } catch (SQLException ex) {
            throw new AppServerException("Cannot get log_id=" + id + " because: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Log> list() {
        try (Connection c = pool.getConnection()) {
            DSLContext context = DSL.using(c, dialect);
            RecordsToListHandler r2lHandler = new RecordsToListHandler();
            return context.selectFrom(LOG).fetchInto(r2lHandler).getLogs();
        } catch (SQLException ex) {
            throw new AppServerException("Cannot get logs list");
        }
    }

    @Override
    public void delete(long id) {
        try (Connection c = pool.getConnection()) {
            DSLContext context = DSL.using(c, dialect);

            context.delete(LOG).where(LOG.ID.eq(id)).execute();
        } catch (SQLException ex) {
            throw new AppServerException("Failed to delete log_id=" + id
                    + " because: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Log update(Log source) {
        if (source == null) {
            throw new NullPointerException("No log information was specified.");
        }

        LOGGER.info("Updating entry in DB...");
        try (Connection c = pool.getConnection()) {
            DSLContext context = DSL.using(c, dialect);

            UpdateQuery<LogRecord> uq = context.updateQuery(LOG);
            if (source.getStatus() != null) {
                uq.addValue(LOG.STATUS, source.getStatus().ordinal());
            }
            if (source.getUriName() != null) {
                uq.addValue(LOG.URI_NAME, source.getUriName());
            }
            if (source.getDataFile() != null) {
                uq.addValue(LOG.DATA_FILE, source.getDataFile());
            }
            if (source.getNotes() != null) {
                uq.addValue(LOG.NOTES, source.getNotes());
            }
            if (source.getName() != null) {
                uq.addValue(LOG.NAME, source.getName());
            }
            if (source.getNotes() != null) {
                uq.addValue(LOG.NOTES, source.getNotes());
            }
            uq.addConditions(LOG.ID.eq(source.getId()));
            uq.setReturning();
            uq.execute();
            LogRecord result = uq.getReturnedRecord();
            LOGGER.info("...Updated entry in DB.");
            return R2L.map(result);
        } catch (SQLException ex) {
            throw new AppServerException("Failed to update log: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void updateStatus(long id, Status newStatus) {
        LOGGER.info("Updating log id={} status={}...", id, newStatus);
        try (Connection c = pool.getConnection()) {
            DSLContext context = DSL.using(c, dialect);

            context.update(LOG)
                    .set(LOG.STATUS, newStatus.ordinal())
                    .where(LOG.ID.eq(id))
                    .execute();
            LOGGER.info("...Updated log id={} status={}.", id, newStatus);
        } catch (SQLException ex) {
            LOGGER.error("...Failed to update log id={} status={}.", id, newStatus);
            throw new AppServerException("Failed to update log: " + ex.getMessage(), ex);
        }
    }

    private static class RecordToLogMapper implements RecordMapper<LogRecord, Log> {

        @Override
        public Log map(LogRecord record) {
            if (record == null) {
                return null;
            }
            return new Log(record.getId(),
                    Status.values()[record.getStatus()],
                    record.getUriName(),
                    record.getName(),
                    record.getDataFile(),
                    record.getNotes()
            );
        }
    }

    private static class RecordsToListHandler implements RecordHandler<LogRecord> {

        private final List<Log> logs = new ArrayList<>();

        @Override
        public void next(LogRecord record) {
            logs.add(R2L.map(record));
        }

        public List<Log> getLogs() {
            return logs;
        }
    }

}
