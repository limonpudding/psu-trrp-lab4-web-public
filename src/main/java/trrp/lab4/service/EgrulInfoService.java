package trrp.lab4.service;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.stereotype.Service;
import trrp.lab4.db.DataSourcePostgres;
import trrp.lab4.dto.EgrulInfoDTO;
import trrp.lab4.generated.Sequences;
import trrp.lab4.generated.tables.daos.EgrulInfoDao;
import trrp.lab4.generated.tables.pojos.EgrulInfo;
import trrp.lab4.generated.tables.records.EgrulInfoRecord;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.SQLDialect.POSTGRES;
import static trrp.lab4.generated.tables.EgrulInfo.EGRUL_INFO;
import static trrp.lab4.generated.tables.EgrulActivities.EGRUL_ACTIVITIES;


@Service
public class EgrulInfoService {

    DSLContext context;
    private EgrulInfoDao dao;

    private final DataSourcePostgres dataSourcePostgres;

    private void prepareConnection(DataSourcePostgres dataSourcePostgres) {
        Connection connection = dataSourcePostgres.getConnection();
        context = DSL.using(connection, POSTGRES);
        Configuration configuration = new DefaultConfiguration().set(connection).set(POSTGRES);
        dao.setConfiguration(configuration);
    }

    public EgrulInfoService(DataSourcePostgres dataSourcePostgres) {
        dao = new EgrulInfoDao();
        prepareConnection(dataSourcePostgres);
        this.dataSourcePostgres = dataSourcePostgres;
    }

    public Integer insert(EgrulInfo egrulInfo) {
        prepareConnection(dataSourcePostgres);
        EgrulInfoRecord egrulInfoRecord = context.selectFrom(EGRUL_INFO)
                .where(EGRUL_INFO.OGRN.eq(egrulInfo.getOgrn()))
                .and(EGRUL_INFO.ACTIVITY_CODE.eq(egrulInfo.getActivityCode()))
                .and(EGRUL_INFO.IS_MAIN.eq(egrulInfo.getIsMain())).fetchOne();
        if (egrulInfoRecord != null) {
            return (Integer) egrulInfoRecord.getId();
        } else {
            Integer id = context.nextval(Sequences.EGRUL_INFO_ID_SEQ);
            egrulInfo.setId(id);
            dao.insert(egrulInfo);
            return id;
        }
    }

    public List<EgrulInfo> getAll() {
        prepareConnection(dataSourcePostgres);
        return context.selectFrom(EGRUL_INFO).fetchInto(EgrulInfo.class);
    }

    public List<EgrulInfoDTO> getIntoDto(String ogrn) {
        prepareConnection(dataSourcePostgres);
        Result<Record4<Integer, String, Boolean, String>> result = context.select(EGRUL_INFO.ID, EGRUL_INFO.ACTIVITY_CODE, EGRUL_INFO.IS_MAIN, EGRUL_ACTIVITIES.DESCRIPTION)
                .from(EGRUL_INFO)
                .join(EGRUL_ACTIVITIES).on(EGRUL_INFO.ACTIVITY_CODE.eq(EGRUL_ACTIVITIES.CODE))
                .where(EGRUL_INFO.OGRN.eq(ogrn)).fetch();
        List<EgrulInfoDTO> egrulInfoDTOList = new ArrayList<>();
        for (Record4 record4:result) {
            EgrulInfoDTO egrulInfoDTO = new EgrulInfoDTO();
            egrulInfoDTO.setId((Integer)record4.get(0));
            egrulInfoDTO.setOgrn(ogrn);
            egrulInfoDTO.setActivityCode((String)record4.get(1));
            egrulInfoDTO.setIsMain((Boolean)record4.get(2));
            egrulInfoDTO.setDescription((String)record4.get(3));
            egrulInfoDTOList.add(egrulInfoDTO);
        }
        return egrulInfoDTOList;
    }
}
