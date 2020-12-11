package trrp.lab4.service;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.stereotype.Service;
import trrp.lab4.db.DataSourcePostgres;
import trrp.lab4.generated.tables.daos.EgrulActivitiesDao;
import trrp.lab4.generated.tables.pojos.EgrulActivities;
import trrp.lab4.generated.tables.records.EgrulActivitiesRecord;

import java.sql.Connection;
import java.util.List;

import static org.jooq.SQLDialect.POSTGRES;
import static trrp.lab4.generated.tables.EgrulActivities.EGRUL_ACTIVITIES;

@Service
public class EgrulActivitiesService {

    DSLContext context;
    private EgrulActivitiesDao dao;

    private final DataSourcePostgres dataSourcePostgres;

    private void prepareConnection(DataSourcePostgres dataSourcePostgres) {
        Connection connection = dataSourcePostgres.getConnection();
        context = DSL.using(connection, POSTGRES);
        Configuration configuration = new DefaultConfiguration().set(connection).set(POSTGRES);
        dao.setConfiguration(configuration);
    }

    public EgrulActivitiesService(DataSourcePostgres dataSourcePostgres) {
        dao = new EgrulActivitiesDao();
        prepareConnection(dataSourcePostgres);
        this.dataSourcePostgres = dataSourcePostgres;
    }

    public String insert(EgrulActivities egrulActivities) {
        prepareConnection(dataSourcePostgres);
        EgrulActivitiesRecord egrulActivitiesRecord = context.selectFrom(EGRUL_ACTIVITIES)
                .where(EGRUL_ACTIVITIES.CODE.eq(egrulActivities.getCode())).fetchOne();
        if (egrulActivitiesRecord == null) {
            dao.insert(egrulActivities);
        }
        return egrulActivities.getCode();
    }

    public List<EgrulActivities> getAll() {
        prepareConnection(dataSourcePostgres);
        Connection connection = dataSourcePostgres.getConnection();
        context = DSL.using(connection, POSTGRES);
        return context.selectFrom(EGRUL_ACTIVITIES).fetchInto(EgrulActivities.class);
    }
}
