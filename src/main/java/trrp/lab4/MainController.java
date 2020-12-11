package trrp.lab4;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import trrp.lab4.dto.EgrulUsersDTO;
import trrp.lab4.generated.tables.pojos.EgrulUsers;
import trrp.lab4.service.EgrulUsersService;
import trrp.lab4.service.EgrulWorkerConnector;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Logger;

@Controller
public class MainController {

    Logger LOGGER = Logger.getLogger(MainController.class.getName());

    public static final String INDEX_PAGE_GET_REQUEST_LOG = "Пользователь перешёл на страницу добавления данных в систему. | IP адрес: {0} | ID сессии: {1}";
    public static final String INDEX_PAGE_POST_REQUEST_LOG = "Пользователь произвёл попытку добавления данных в систему. | IP адрес: {0} | ID сессии: {1}";
    public static final String SEARCH_PAGE_GET_REQUEST_LOG = "Пользователь перешёл на страницу посика и просмотра данных в системе. | IP адрес: {0} | ID сессии: {1}";
    public static final String SEARCH_PAGE_POST_REQUEST_LOG = "Пользователь произвёл попытку поиска данных в системе. | IP адрес: {0} | ID сессии: {1}";

    @Autowired
    private EgrulUsersService egrulUsersService;

    @Autowired
    private EgrulWorkerConnector egrulWorkerConnector;

    @RequestMapping(value = { "/index" }, method = RequestMethod.GET)
    public ModelAndView viewHome(HttpServletRequest request) {
        LOGGER.info(MessageFormat.format(INDEX_PAGE_GET_REQUEST_LOG, getClientAddress(request), request.getSession().getId()));
        return buildIndexMAV(new EgrulUsersDTO(), false, "none");
    }

    @RequestMapping(value = { "/index" }, method = RequestMethod.POST)
    public ModelAndView addEgrul(HttpServletRequest request,
                                 @ModelAttribute(name = "egrulUser") EgrulUsersDTO egrulUser) throws IOException {
        LOGGER.info(MessageFormat.format(INDEX_PAGE_POST_REQUEST_LOG, getClientAddress(request), request.getSession().getId()));
        ModelAndView mav;
        EgrulUsersDTO egrulUsersDTO;
        EgrulUsers userFromDB = egrulUsersService.getByOGRN(egrulUser.getOgrn());
        if (userFromDB != null) {
            egrulUsersDTO = egrulUsersService.getIntoDto(egrulUser.getOgrn());
            mav = buildIndexMAV(egrulUsersDTO, true, "alreadyExists");
        } else {
            egrulUsersDTO = egrulWorkerConnector.getEgrulDataFromWorker(egrulUser.getOgrn());
            if (egrulUsersDTO != null) {
                egrulUsersService.saveFromDTO(egrulUsersDTO);
                mav = buildIndexMAV(egrulUsersDTO, true, "successfullyAdded");
            } else {
                mav = buildIndexMAV(new EgrulUsersDTO(), true, "notFoundInfo");
            }
        }
        return mav;
    }

    private ModelAndView buildIndexMAV(EgrulUsersDTO dto, boolean printResultInfo, String status) {
        ModelAndView mav = new ModelAndView("index");
        mav.addObject("egrulUser", dto);
        mav.addObject("printResultInfo", printResultInfo);
        mav.addObject("status", status);
        return mav;
    }

    @RequestMapping(value = { "/search" }, method = RequestMethod.GET)
    public ModelAndView searchEgrul(HttpServletRequest request) {
        LOGGER.info(MessageFormat.format(SEARCH_PAGE_GET_REQUEST_LOG, getClientAddress(request), request.getSession().getId()));
        return buildSearchMAV(new EgrulUsersDTO(), false, false);
    }

    @RequestMapping(value = { "/search" }, method = RequestMethod.POST)
    public ModelAndView searchEgrul(HttpServletRequest request,
                                    @ModelAttribute(name = "egrulUser") EgrulUsersDTO egrulUser) {
        LOGGER.info(MessageFormat.format(SEARCH_PAGE_POST_REQUEST_LOG, getClientAddress(request), request.getSession().getId()));
        ModelAndView mav;
        EgrulUsersDTO egrulUsersDTO;
        EgrulUsers userFromDB = egrulUsersService.getByOGRN(egrulUser.getOgrn());
        if (userFromDB != null) {
            egrulUsersDTO = egrulUsersService.getIntoDto(egrulUser.getOgrn());
            mav = buildSearchMAV(egrulUsersDTO, true, true);
        } else {
            mav = buildSearchMAV(new EgrulUsersDTO(), false, true);
        }
        return mav;
    }

    private ModelAndView buildSearchMAV(EgrulUsersDTO dto, boolean found, boolean printData) {
        ModelAndView mav = new ModelAndView("search");
        mav.addObject("egrulUser", dto);
        mav.addObject("found", found);
        mav.addObject("printData", printData);
        return mav;
    }

    private String getClientAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
