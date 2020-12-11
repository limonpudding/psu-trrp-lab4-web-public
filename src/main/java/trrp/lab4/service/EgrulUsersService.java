package trrp.lab4.service;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.stereotype.Service;
import trrp.lab4.MainController;
import trrp.lab4.db.DataSourcePostgres;
import trrp.lab4.dto.EgrulInfoDTO;
import trrp.lab4.dto.EgrulUsersDTO;
import trrp.lab4.generated.tables.daos.EgrulUsersDao;
import trrp.lab4.generated.tables.pojos.EgrulActivities;
import trrp.lab4.generated.tables.pojos.EgrulInfo;
import trrp.lab4.generated.tables.pojos.EgrulUsers;
import trrp.lab4.generated.tables.records.EgrulUsersRecord;

import java.sql.Connection;
import java.util.List;
import java.util.logging.Logger;

import static org.jooq.SQLDialect.POSTGRES;
import static trrp.lab4.generated.tables.EgrulUsers.EGRUL_USERS;

@Service
public class EgrulUsersService {

    Logger LOGGER = Logger.getLogger(EgrulUsersService.class.getName());

    DSLContext context;
    private EgrulUsersDao dao;

    private final DataSourcePostgres dataSourcePostgres;

    private final EgrulActivitiesService egrulActivitiesService;

    private final EgrulInfoService egrulInfoService;

    private void prepareConnection(DataSourcePostgres dataSourcePostgres) {
        Connection connection = dataSourcePostgres.getConnection();
        context = DSL.using(connection, POSTGRES);
        Configuration configuration = new DefaultConfiguration().set(connection).set(POSTGRES);
        dao.setConfiguration(configuration);
    }

    public EgrulUsersService(DataSourcePostgres dataSourcePostgres, EgrulActivitiesService egrulActivitiesService, EgrulInfoService egrulInfoService) {
        dao = new EgrulUsersDao();
        prepareConnection(dataSourcePostgres);
        this.dataSourcePostgres = dataSourcePostgres;
        this.egrulActivitiesService = egrulActivitiesService;
        this.egrulInfoService = egrulInfoService;
    }

    public String insert(EgrulUsers egrulUsers) {
        prepareConnection(dataSourcePostgres);
        EgrulUsersRecord egrulUsersRecord = context.selectFrom(EGRUL_USERS)
                .where(EGRUL_USERS.OGRN.eq(egrulUsers.getOgrn())).fetchOne();
        if (egrulUsersRecord == null) {
            dao.insert(egrulUsers);
        }
        return egrulUsers.getOgrn();
    }

    public List<EgrulUsers> getAll() {
        prepareConnection(dataSourcePostgres);
        return context.selectFrom(EGRUL_USERS).fetchInto(EgrulUsers.class);
    }

    public EgrulUsers getByOGRN(String ogrn) {
        prepareConnection(dataSourcePostgres);
        return dao.fetchOneByOgrn(ogrn);
    }

    public Integer saveFromDTO(EgrulUsersDTO dto) {
        prepareConnection(dataSourcePostgres);
        EgrulUsers egrulUsers = new EgrulUsers();
        egrulUsers.setOgrn(dto.getOgrn());
        egrulUsers.setFirstName(dto.getFirstName());
        egrulUsers.setLastName(dto.getLastName());
        egrulUsers.setPatronymic(dto.getPatronymic());
        insert(egrulUsers);

        for (EgrulInfoDTO info:dto.getEgrulInfoDTOS()) {
            EgrulActivities activity = new EgrulActivities();
            activity.setCode(info.getActivityCode());
            activity.setDescription(info.getDescription());
            egrulActivitiesService.insert(activity);

            EgrulInfo egrulInfo = new EgrulInfo();
            egrulInfo.setOgrn(dto.getOgrn());
            egrulInfo.setActivityCode(info.getActivityCode());
            egrulInfo.setIsMain(info.getIsMain());
            egrulInfoService.insert(egrulInfo);
        }
        LOGGER.info("Данные по ОГРН " + dto.getOgrn() + " спешно загружены в систему!");
        return 1;
    }

    public EgrulUsersDTO getIntoDto(String ogrn) {
        prepareConnection(dataSourcePostgres);
        EgrulUsers egrulUsers = getByOGRN(ogrn);
        EgrulUsersDTO egrulUserDTO = new EgrulUsersDTO();
        egrulUserDTO.setOgrn(ogrn);
        egrulUserDTO.setFirstName(egrulUsers.getFirstName());
        egrulUserDTO.setLastName(egrulUsers.getLastName());
        egrulUserDTO.setPatronymic(egrulUsers.getPatronymic());

        List<EgrulInfoDTO> egrulInfos = egrulInfoService.getIntoDto(ogrn);
        egrulUserDTO.setEgrulInfoDTOS(egrulInfos);
        return egrulUserDTO;
    }
}
