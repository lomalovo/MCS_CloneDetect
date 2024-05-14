package com.googlecode.openmpis.action;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.upload.FormFile;
import com.googlecode.openmpis.dto.Log;
import com.googlecode.openmpis.dto.Person;
import com.googlecode.openmpis.dto.User;
import com.googlecode.openmpis.form.PersonForm;
import com.googlecode.openmpis.persistence.ibatis.dao.impl.AbductorDAOImpl;
import com.googlecode.openmpis.persistence.ibatis.dao.impl.LogDAOImpl;
import com.googlecode.openmpis.persistence.ibatis.dao.impl.MessageDAOImpl;
import com.googlecode.openmpis.persistence.ibatis.dao.impl.PersonDAOImpl;
import com.googlecode.openmpis.persistence.ibatis.dao.impl.RelativeDAOImpl;
import com.googlecode.openmpis.persistence.ibatis.dao.impl.ReportDAOImpl;
import com.googlecode.openmpis.persistence.ibatis.dao.impl.UserDAOImpl;
import com.googlecode.openmpis.persistence.ibatis.service.AbductorService;
import com.googlecode.openmpis.persistence.ibatis.service.LogService;
import com.googlecode.openmpis.persistence.ibatis.service.MessageService;
import com.googlecode.openmpis.persistence.ibatis.service.PersonService;
import com.googlecode.openmpis.persistence.ibatis.service.RelativeService;
import com.googlecode.openmpis.persistence.ibatis.service.ReportService;
import com.googlecode.openmpis.persistence.ibatis.service.UserService;
import com.googlecode.openmpis.persistence.ibatis.service.impl.AbductorServiceImpl;
import com.googlecode.openmpis.persistence.ibatis.service.impl.LogServiceImpl;
import com.googlecode.openmpis.persistence.ibatis.service.impl.MessageServiceImpl;
import com.googlecode.openmpis.persistence.ibatis.service.impl.PersonServiceImpl;
import com.googlecode.openmpis.persistence.ibatis.service.impl.RelativeServiceImpl;
import com.googlecode.openmpis.persistence.ibatis.service.impl.ReportServiceImpl;
import com.googlecode.openmpis.persistence.ibatis.service.impl.UserServiceImpl;
import com.googlecode.openmpis.util.Constants;
import com.googlecode.openmpis.util.Validator;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * The PersonAction class provides the methods to list, add, edit, delete and view
 * persons.
 *
 * @author  <a href="mailto:rvbabilonia@gmail.com">Rey Vincent Babilonia</a>
 * @version 1.0
 */
public class PersonAction extends DispatchAction {

    /**
     * The person service
     */
    private PersonService personService = new PersonServiceImpl(new PersonDAOImpl());

    /**
     * The report service
     */
    private ReportService reportService = new ReportServiceImpl(new ReportDAOImpl());

    /**
     * The message service
     */
    private MessageService messageService = new MessageServiceImpl(new MessageDAOImpl());

    /**
     * The relative service
     */
    private RelativeService relativeService = new RelativeServiceImpl(new RelativeDAOImpl());

    /**
     * The abductor service
     */
    private AbductorService abductorService = new AbductorServiceImpl(new AbductorDAOImpl());

    /**
     * The user service
     */
    private UserService userService = new UserServiceImpl(new UserDAOImpl());

    /**
     * The log service
     */
    private LogService logService = new LogServiceImpl(new LogDAOImpl());

    /**
     * The file logger
     */
    private Logger logger = Logger.getLogger(this.getClass());

    /**
     * The format for date (e.g. 2009-02-28)
     */
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Prepares the form for person creation.
     * This is the new person action called from the Struts framework.
     *
     * @param mapping       the ActionMapping used to select this instance
     * @param form          the optional ActionForm bean for this request
     * @param request       the HTTP Request we are processing
     * @param response      the HTTP Response we are processing
     * @return              the forwarding instance
     */
    public ActionForward newPerson(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        User currentUser = null;
        if (request.getSession().getAttribute("currentuser") == null) {
            return mapping.findForward(Constants.EXPIRED);
        } else {
            currentUser = (User) request.getSession().getAttribute("currentuser");
        }
        if (currentUser.getGroupId() == 1) {
            request.setAttribute("action", request.getParameter("action"));
            return mapping.findForward(Constants.ADD_PERSON);
        } else {
            return mapping.findForward(Constants.UNAUTHORIZED);
        }
    }

    /**
     * Inserts a person into the database.
     * This is the add person action called from the HTML form.
     *
     * @param mapping       the ActionMapping used to select this instance
     * @param form          the optional ActionForm bean for this request
     * @param request       the HTTP Request we are processing
     * @param response      the HTTP Response we are processing
     * @return              the forwarding instance
     * @throws java.lang.Exception
     */
    public ActionForward addPerson(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        User currentUser = null;
        if (request.getSession().getAttribute("currentuser") == null) {
            return mapping.findForward(Constants.EXPIRED);
        } else {
            currentUser = (User) request.getSession().getAttribute("currentuser");
        }
        if (currentUser.getGroupId() == 1) {
            PersonForm personForm = (PersonForm) form;
            ActionMessages errors = new ActionMessages();
            request.setAttribute("action", request.getParameter("action"));
            if (isValidPerson(request, form)) {
                Person checker = new Person();
                String firstName = personForm.getFirstName();
                String nickname = personForm.getNickname();
                String middleName = personForm.getMiddleName();
                String lastName = personForm.getLastName();
                checker.setFirstName(firstName);
                checker.setNickname(nickname);
                checker.setMiddleName(middleName);
                checker.setLastName(lastName);
                if (personService.isUniquePerson(checker)) {
                    Person person = new Person();
                    person.setStatus(personForm.getStatus());
                    person.setType(personForm.getType());
                    person.setFirstName(firstName);
                    person.setNickname(nickname);
                    person.setMiddleName(middleName);
                    person.setLastName(lastName);
                    if (personForm.isKnownBirthDate()) {
                        person.setBirthMonth(personForm.getBirthMonth());
                        person.setBirthDay(personForm.getBirthDay());
                        person.setBirthYear(personForm.getBirthYear());
                    }
                    person.setStreet(personForm.getStreet());
                    person.setCity(personForm.getCity());
                    person.setProvince(personForm.getProvince());
                    person.setCountry(personForm.getCountry());
                    person.setSex(personForm.getSex());
                    person.setFeet(personForm.getFeet());
                    person.setInches(personForm.getInches());
                    person.setWeight(personForm.getWeight());
                    person.setReligion(personForm.getReligion());
                    person.setRace(personForm.getRace());
                    person.setEyeColor(personForm.getEyeColor());
                    person.setHairColor(personForm.getHairColor());
                    person.setMedicalCondition(personForm.getMedicalCondition());
                    person.setMarks(personForm.getMarks());
                    person.setPersonalEffects(personForm.getPersonalEffects());
                    person.setRemarks(personForm.getRemarks());
                    person.setMonthMissingOrFound(personForm.getMonthMissingOrFound());
                    person.setDayMissingOrFound(personForm.getDayMissingOrFound());
                    person.setYearMissingOrFound(personForm.getYearMissingOrFound());
                    if (person.getType() <= 4) {
                        person.setMissingFromCity(personForm.getMissingFromCity());
                        person.setMissingFromProvince(personForm.getMissingFromProvince());
                        person.setMissingFromCountry(personForm.getMissingFromCountry());
                        person.setPossibleCity(personForm.getPossibleCity());
                        person.setPossibleProvince(personForm.getPossibleProvince());
                        person.setPossibleCountry(personForm.getPossibleCountry());
                        person.setCircumstance(personForm.getCircumstance());
                        person.setReward(personForm.getReward());
                    } else {
                        person.setInstitution(personForm.getInstitution());
                        person.setInstitutionStreet(personForm.getInstitutionStreet());
                        person.setInstitutionCity(personForm.getInstitutionCity());
                        person.setInstitutionProvince(personForm.getInstitutionProvince());
                        person.setInstitutionCountry(personForm.getInstitutionCountry());
                        person.setInstitutionEmail(personForm.getInstitutionEmail());
                        person.setInstitutionNumber(personForm.getInstitutionNumber());
                    }
                    if (!personForm.getCodisId().isEmpty()) {
                        person.setCodisId(personForm.getCodisId());
                    }
                    if (!personForm.getAfisId().isEmpty()) {
                        person.setAfisId(personForm.getAfisId());
                    }
                    if (!personForm.getDentalId().isEmpty()) {
                        person.setDentalId(personForm.getDentalId());
                    }
                    String date = simpleDateFormat.format(System.currentTimeMillis());
                    person.setDate(date);
                    person.setEncoderId(currentUser.getId());
                    int generatedId = personService.insertPerson(person);
                    if (generatedId > 0) {
                        FormFile photoFile = personForm.getPhotoFile();
                        FormFile agedPhotoFile = personForm.getAgedPhotoFile();
                        if ((photoFile.getFileName().length() > 0) || (agedPhotoFile.getFileName().length() > 0)) {
                            String contextUnknownPhotoFilename = "photo/unknown.png";
                            String contextDefaultPhotoFilename = contextUnknownPhotoFilename;
                            String contextAgedPhotoFilename = contextUnknownPhotoFilename;
                            String tokens[] = photoFile.getFileName().toLowerCase().split("\\.");
                            String extensionName = tokens[1];
                            if (agedPhotoFile.getFileName().length() > 0) {
                                tokens = agedPhotoFile.getFileName().toLowerCase().split("\\.");
                                extensionName = tokens[1];
                            }
                            String directoryName = createDirectoryName(generatedId);
                            int age = getAge(personForm.getBirthMonth() - 1, personForm.getBirthDay(), personForm.getBirthYear());
                            String contextPhotoDirectory = "photo/" + directoryName;
                            String contextDefaultPhotoDirectory = contextPhotoDirectory + "/default";
                            String contextAgedPhotoDirectory = contextPhotoDirectory + "/aged";
                            String absolutePhotoDirectory = getServlet().getServletContext().getRealPath("/") + "photo" + File.separator + directoryName;
                            String absoluteDefaultPhotoDirectory = absolutePhotoDirectory + File.separator + "default";
                            String absoluteAgedPhotoDirectory = absolutePhotoDirectory + File.separator + "aged";
                            File photoDirectory = new File(absolutePhotoDirectory);
                            File defaultPhotoDirectory = new File(absoluteDefaultPhotoDirectory);
                            File agedPhotoDirectory = new File(absoluteAgedPhotoDirectory);
                            if (!photoDirectory.exists()) {
                                photoDirectory.mkdir();
                                defaultPhotoDirectory.mkdir();
                                agedPhotoDirectory.mkdir();
                            } else {
                                if ((!defaultPhotoDirectory.exists()) || (!agedPhotoDirectory.exists())) {
                                    defaultPhotoDirectory.mkdir();
                                    agedPhotoDirectory.mkdir();
                                }
                            }
                            String absoluteDefaultPhotoFilename = absoluteDefaultPhotoDirectory + File.separator + directoryName + "-age-" + age + "." + extensionName;
                            contextDefaultPhotoFilename = contextDefaultPhotoDirectory + "/" + directoryName + "-age-" + age + "." + extensionName;
                            File file = new File(absoluteDefaultPhotoFilename);
                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write(photoFile.getFileData());
                            fos.close();
                            fos.flush();
                            if (agedPhotoFile.getFileName().length() > 0) {
                                String absoluteAgedPhotoFilename = absoluteAgedPhotoDirectory + File.separator + directoryName + "." + extensionName;
                                contextAgedPhotoFilename = contextAgedPhotoDirectory + "/" + directoryName + "." + extensionName;
                                file = new File(absoluteAgedPhotoFilename);
                                fos = new FileOutputStream(file);
                                fos.write(agedPhotoFile.getFileData());
                                fos.close();
                                fos.flush();
                            }
                            person.setId(generatedId);
                            person.setPhoto(contextDefaultPhotoFilename);
                            if (agedPhotoFile.getFileName().length() > 0) {
                                person.setAgedPhoto(contextAgedPhotoFilename);
                            } else {
                                person.setAgedPhoto(contextUnknownPhotoFilename);
                            }
                            personService.updatePerson(person);
                        }
                        Log addLog = new Log();
                        if ((!firstName.isEmpty()) && (!lastName.isEmpty())) {
                            addLog.setLog(firstName + " '" + personForm.getNickname() + "' " + lastName + " was encoded by " + currentUser.getUsername() + ".");
                        } else {
                            addLog.setLog("' " + personForm.getNickname() + " '" + " was encoded by " + currentUser.getUsername() + ".");
                        }
                        addLog.setDate(date);
                        logService.insertLog(addLog);
                        logger.info(addLog.toString());
                        if (person.getType() > 4) {
                            request.setAttribute("personid", generatedId);
                            return mapping.findForward(Constants.SELECT_INVESTIGATOR);
                        } else {
                            request.setAttribute("personid", generatedId);
                            return mapping.findForward(Constants.ADD_RELATIVE);
                        }
                    } else {
                        return mapping.findForward(Constants.FAILURE);
                    }
                } else {
                    errors.add("person", new ActionMessage("error.person.duplicate"));
                    saveErrors(request, errors);
                    logger.error("Duplicate person.");
                    return mapping.findForward(Constants.ADD_PERSON_REDO);
                }
            } else {
                return mapping.findForward(Constants.ADD_PERSON_REDO);
            }
        } else {
            return mapping.findForward(Constants.UNAUTHORIZED);
        }
    }

    /**
     * Retrieves a person from the database.
     * This is the view person action called from the Struts framework.
     *
     * @param mapping       the ActionMapping used to select this instance
     * @param form          the optional ActionForm bean for this request
     * @param request       the HTTP Request we are processing
     * @param response      the HTTP Response we are processing
     * @return              the forwarding instance
     * @throws java.lang.Exception
     */
    public ActionForward viewPerson(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        User currentUser = null;
        PersonForm personForm = (PersonForm) form;
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Person person = (Person) personService.getPersonById(id);
            if (person.getPhoto() != null) {
                personForm.setPhoto(person.getPhoto());
            }
            if (person.getAgedPhoto() != null) {
                personForm.setAgedPhoto(person.getAgedPhoto());
            }
            personForm.setId(person.getId());
            personForm.setStatus(person.getStatus());
            personForm.setType(person.getType());
            personForm.setFirstName(person.getFirstName());
            personForm.setNickname(person.getNickname());
            personForm.setMiddleName(person.getMiddleName());
            personForm.setLastName(person.getLastName());
            personForm.setBirthMonth(person.getBirthMonth());
            personForm.setBirthDay(person.getBirthDay());
            personForm.setBirthYear(person.getBirthYear());
            if (person.getBirthMonth() != 0) {
                personForm.setKnownBirthDate(true);
            }
            personForm.setAge(getAge(person.getBirthMonth() - 1, person.getBirthDay(), person.getBirthYear()));
            personForm.setStreet(person.getStreet());
            personForm.setCity(person.getCity());
            personForm.setProvince(person.getProvince());
            personForm.setCountry(person.getCountry());
            personForm.setSex(person.getSex());
            personForm.setFeet(person.getFeet());
            personForm.setInches(person.getInches());
            personForm.setWeight(person.getWeight());
            personForm.setReligion(person.getReligion());
            personForm.setRace(person.getRace());
            personForm.setEyeColor(person.getEyeColor());
            personForm.setHairColor(person.getHairColor());
            personForm.setMedicalCondition(person.getMedicalCondition());
            personForm.setMarks(person.getMarks());
            personForm.setPersonalEffects(person.getPersonalEffects());
            personForm.setRemarks(person.getRemarks());
            personForm.setMonthMissingOrFound(person.getMonthMissingOrFound());
            personForm.setDayMissingOrFound(person.getDayMissingOrFound());
            personForm.setYearMissingOrFound(person.getYearMissingOrFound());
            personForm.setDaysMissing(getDaysMissing(person.getMonthMissingOrFound() - 1, person.getDayMissingOrFound(), person.getYearMissingOrFound()));
            if (person.getMissingFromCity() != null) {
                personForm.setMissingFromCity(person.getMissingFromCity());
            }
            if (person.getMissingFromProvince() != null) {
                personForm.setMissingFromProvince(person.getMissingFromProvince());
            }
            if (person.getMissingFromCountry() != null) {
                personForm.setMissingFromCountry(person.getMissingFromCountry());
            }
            if (person.getPossibleCity() != null) {
                personForm.setPossibleCity(person.getPossibleCity());
            }
            if (person.getPossibleProvince() != null) {
                personForm.setPossibleProvince(person.getPossibleProvince());
            }
            if (person.getPossibleCountry() != null) {
                personForm.setPossibleCountry(person.getPossibleCountry());
            }
            if (person.getCircumstance() != null) {
                personForm.setCircumstance(person.getCircumstance());
            }
            if (person.getReward() != null) {
                personForm.setReward(person.getReward());
            }
            if (person.getInstitution() != null) {
                personForm.setInstitution(person.getInstitution());
            }
            if (person.getInstitutionStreet() != null) {
                personForm.setInstitutionStreet(person.getInstitutionStreet());
            }
            if (person.getInstitutionCity() != null) {
                personForm.setInstitutionCity(person.getInstitutionCity());
            }
            if (person.getInstitutionProvince() != null) {
                personForm.setInstitutionProvince(person.getInstitutionProvince());
            }
            if (person.getInstitutionCountry() != null) {
                personForm.setInstitutionCountry(person.getInstitutionCountry());
            }
            if (person.getInstitutionEmail() != null) {
                personForm.setInstitutionEmail(person.getInstitutionEmail());
            }
            if (person.getInstitutionNumber() != null) {
                personForm.setInstitutionNumber(person.getInstitutionNumber());
            }
            if (person.getCodisId() != null) {
                personForm.setCodisId(person.getCodisId());
            }
            if (person.getAfisId() != null) {
                personForm.setAfisId(person.getAfisId());
            }
            if (person.getDentalId() != null) {
                personForm.setDentalId(person.getDentalId());
            }
            if (person.getRelativeId() != null) {
                personForm.setRelativeId(person.getRelativeId());
                personForm.setRelativeFirstName(relativeService.getRelativeById(person.getRelativeId()).getFirstName());
                personForm.setRelativeLastName(relativeService.getRelativeById(person.getRelativeId()).getLastName());
            }
            if (person.getInvestigatorId() != null) {
                personForm.setInvestigatorId(person.getInvestigatorId());
                personForm.setInvestigatorUsername(userService.getUserById(person.getInvestigatorId()).getUsername());
            }
            if (person.getAbductorId() != null) {
                personForm.setAbductorId(person.getAbductorId());
                personForm.setAbductorFirstName(abductorService.getAbductorById(person.getAbductorId()).getFirstName());
                personForm.setAbductorLastName(abductorService.getAbductorById(person.getAbductorId()).getLastName());
            }
            personForm.setProgressReports(reportService.countAllReportsForPerson(person.getId()));
            if (request.getSession().getAttribute("currentuser") != null) {
                currentUser = (User) request.getSession().getAttribute("currentuser");
                request.setAttribute("action", request.getParameter("action"));
                if (currentUser.getGroupId() == 1) {
                    return mapping.findForward(Constants.EDIT_PERSON);
                } else {
                    return mapping.findForward(Constants.VIEW_PERSON);
                }
            } else {
                return mapping.findForward(Constants.VIEW_PERSON);
            }
        } catch (NumberFormatException nfe) {
            return mapping.findForward(Constants.LIST_PERSON);
        }
    }

    /**
     * Updates a person.
     * This is the edit person action called from the HTML form.
     *
     * @param mapping       the ActionMapping used to select this instance
     * @param form          the optional ActionForm bean for this request
     * @param request       the HTTP Request we are processing
     * @param response      the HTTP Response we are processing
     * @return              the forwarding instance
     * @throws java.lang.Exception
     */
    public ActionForward editPerson(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        User currentUser = null;
        if (request.getSession().getAttribute("currentuser") == null) {
            return mapping.findForward(Constants.EXPIRED);
        } else {
            currentUser = (User) request.getSession().getAttribute("currentuser");
        }
        if (currentUser.getGroupId() == 1) {
            PersonForm personForm = (PersonForm) form;
            ActionMessages errors = new ActionMessages();
            request.setAttribute("action", request.getParameter("action"));
            Person person = personService.getPersonById(personForm.getId());
            if (person.getRelativeId() != null) {
                personForm.setRelativeId(person.getRelativeId());
                personForm.setRelativeFirstName(relativeService.getRelativeById(person.getRelativeId()).getFirstName());
                personForm.setRelativeLastName(relativeService.getRelativeById(person.getRelativeId()).getLastName());
            }
            if (person.getInvestigatorId() != null) {
                personForm.setInvestigatorId(person.getInvestigatorId());
                personForm.setInvestigatorUsername(userService.getUserById(person.getInvestigatorId()).getUsername());
            }
            if (person.getAbductorId() != null) {
                personForm.setAbductorId(person.getAbductorId());
                personForm.setAbductorFirstName(abductorService.getAbductorById(person.getAbductorId()).getFirstName());
                personForm.setAbductorLastName(abductorService.getAbductorById(person.getAbductorId()).getLastName());
            }
            personForm.setProgressReports(reportService.countAllReportsForPerson(person.getId()));
            if (isValidPerson(request, form)) {
                Person checker = new Person();
                String firstName = personForm.getFirstName();
                String lastName = personForm.getLastName();
                checker.setId(personForm.getId());
                checker.setFirstName(firstName);
                checker.setLastName(lastName);
                if (personService.isUniquePerson(checker)) {
                    FormFile photoFile = personForm.getPhotoFile();
                    FormFile agedPhotoFile = personForm.getAgedPhotoFile();
                    String contextUnknownPhotoFilename = "photo/unknown.png";
                    String contextDefaultPhotoFilename = contextUnknownPhotoFilename;
                    String contextAgedPhotoFilename = contextUnknownPhotoFilename;
                    if ((photoFile.getFileName().length() > 0) || (agedPhotoFile.getFileName().length() > 0)) {
                        String tokens[];
                        String extensionName = "";
                        if (photoFile.getFileName().length() > 0) {
                            tokens = photoFile.getFileName().toLowerCase().split("\\.");
                            extensionName = tokens[1];
                        }
                        if (agedPhotoFile.getFileName().length() > 0) {
                            tokens = agedPhotoFile.getFileName().toLowerCase().split("\\.");
                            extensionName = tokens[1];
                        }
                        String directoryName = createDirectoryName(personForm.getId());
                        int age = getAge(personForm.getBirthMonth() - 1, personForm.getBirthDay(), personForm.getBirthYear());
                        String contextPhotoDirectory = "photo/" + directoryName;
                        String contextDefaultPhotoDirectory = contextPhotoDirectory + "/default";
                        String contextAgedPhotoDirectory = contextPhotoDirectory + "/aged";
                        String absolutePhotoDirectory = getServlet().getServletContext().getRealPath("/") + "photo" + File.separator + directoryName;
                        String absoluteDefaultPhotoDirectory = absolutePhotoDirectory + File.separator + "default";
                        String absoluteAgedPhotoDirectory = absolutePhotoDirectory + File.separator + "aged";
                        File photoDirectory = new File(absolutePhotoDirectory);
                        File defaultPhotoDirectory = new File(absoluteDefaultPhotoDirectory);
                        File agedPhotoDirectory = new File(absoluteAgedPhotoDirectory);
                        if (!photoDirectory.exists()) {
                            photoDirectory.mkdir();
                            defaultPhotoDirectory.mkdir();
                            agedPhotoDirectory.mkdir();
                        } else {
                            if ((!defaultPhotoDirectory.exists()) || (!agedPhotoDirectory.exists())) {
                                defaultPhotoDirectory.mkdir();
                                agedPhotoDirectory.mkdir();
                            }
                        }
                        if (photoFile.getFileName().length() > 0) {
                            String absoluteDefaultPhotoFilename = absoluteDefaultPhotoDirectory + File.separator + directoryName + "-age-" + age + "." + extensionName;
                            contextDefaultPhotoFilename = contextDefaultPhotoDirectory + "/" + directoryName + "-age-" + age + "." + extensionName;
                            File file = new File(absoluteDefaultPhotoFilename);
                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write(photoFile.getFileData());
                            fos.close();
                            fos.flush();
                        }
                        if (agedPhotoFile.getFileName().length() > 0) {
                            String absoluteAgedPhotoFilename = absoluteAgedPhotoDirectory + File.separator + directoryName + "." + extensionName;
                            contextAgedPhotoFilename = contextAgedPhotoDirectory + "/" + directoryName + "." + extensionName;
                            File file = new File(absoluteAgedPhotoFilename);
                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write(agedPhotoFile.getFileData());
                            fos.close();
                            fos.flush();
                        }
                    }
                    person = new Person();
                    person.setId(personForm.getId());
                    if (photoFile.getFileName().length() > 0) {
                        person.setPhoto(contextDefaultPhotoFilename);
                    } else {
                        person.setPhoto(personForm.getPhoto());
                    }
                    if (agedPhotoFile.getFileName().length() > 0) {
                        person.setAgedPhoto(contextAgedPhotoFilename);
                    }
                    person.setStatus(personForm.getStatus());
                    person.setType(personForm.getType());
                    person.setFirstName(firstName);
                    person.setNickname(personForm.getNickname());
                    person.setMiddleName(personForm.getMiddleName());
                    person.setLastName(lastName);
                    if (personForm.isKnownBirthDate()) {
                        person.setBirthMonth(personForm.getBirthMonth());
                        person.setBirthDay(personForm.getBirthDay());
                        person.setBirthYear(personForm.getBirthYear());
                    }
                    person.setStreet(personForm.getStreet());
                    person.setCity(personForm.getCity());
                    person.setProvince(personForm.getProvince());
                    person.setCountry(personForm.getCountry());
                    person.setSex(personForm.getSex());
                    person.setFeet(personForm.getFeet());
                    person.setInches(personForm.getInches());
                    person.setWeight(personForm.getWeight());
                    person.setReligion(personForm.getReligion());
                    person.setRace(personForm.getRace());
                    person.setEyeColor(personForm.getEyeColor());
                    person.setHairColor(personForm.getHairColor());
                    person.setMedicalCondition(personForm.getMedicalCondition());
                    person.setMarks(personForm.getMarks());
                    person.setPersonalEffects(personForm.getPersonalEffects());
                    person.setRemarks(personForm.getRemarks());
                    person.setMonthMissingOrFound(personForm.getMonthMissingOrFound());
                    person.setDayMissingOrFound(personForm.getDayMissingOrFound());
                    person.setYearMissingOrFound(personForm.getYearMissingOrFound());
                    if (person.getType() <= 4) {
                        person.setMissingFromCity(personForm.getMissingFromCity());
                        person.setMissingFromProvince(personForm.getMissingFromProvince());
                        person.setMissingFromCountry(personForm.getMissingFromCountry());
                        person.setPossibleCity(personForm.getPossibleCity());
                        person.setPossibleProvince(personForm.getPossibleProvince());
                        person.setPossibleCountry(personForm.getPossibleCountry());
                        person.setCircumstance(personForm.getCircumstance());
                        person.setReward(personForm.getReward());
                    } else {
                        person.setInstitution(personForm.getInstitution());
                        person.setInstitutionStreet(personForm.getInstitutionStreet());
                        person.setInstitutionCity(personForm.getInstitutionCity());
                        person.setInstitutionProvince(personForm.getInstitutionProvince());
                        person.setInstitutionCountry(personForm.getInstitutionCountry());
                        person.setInstitutionEmail(personForm.getInstitutionEmail());
                        person.setInstitutionNumber(personForm.getInstitutionNumber());
                    }
                    if (!personForm.getCodisId().isEmpty()) {
                        person.setCodisId(personForm.getCodisId());
                    }
                    if (!personForm.getAfisId().isEmpty()) {
                        person.setAfisId(personForm.getAfisId());
                    }
                    if (!personForm.getDentalId().isEmpty()) {
                        person.setDentalId(personForm.getDentalId());
                    }
                    if (!((personForm.getType() == 1) || (personForm.getType() == 2))) {
                        person.setAbductorId(null);
                        person.setRelationToAbductor(null);
                        personService.updatePersonAbductor(person);
                    }
                    boolean isUpdated = personService.updatePerson(person);
                    person = personService.getPersonById(person.getId());
                    if (isUpdated) {
                        Log editLog = new Log();
                        if ((person.getFirstName().equals(personForm.getFirstName())) && (person.getNickname().equals(personForm.getNickname())) && (person.getMiddleName().equals(personForm.getMiddleName())) && (person.getLastName().equals(personForm.getLastName()))) {
                            editLog.setLog("Person " + person.getNickname() + " was updated by " + currentUser.getUsername() + ".");
                        } else {
                            editLog.setLog("Person " + person.getFirstName() + " '" + person.getNickname() + "' " + person.getLastName() + " was renamed to " + firstName + " '" + personForm.getNickname() + "' " + lastName + " by " + currentUser.getUsername() + ".");
                        }
                        editLog.setDate(simpleDateFormat.format(System.currentTimeMillis()));
                        logService.insertLog(editLog);
                        logger.info(editLog.toString());
                        if (person.getType() > 4) {
                            request.setAttribute("personid", person.getId());
                            return mapping.findForward(Constants.SELECT_INVESTIGATOR);
                        } else {
                            request.setAttribute("personid", person.getId());
                            return mapping.findForward(Constants.ADD_RELATIVE);
                        }
                    } else {
                        return mapping.findForward(Constants.FAILURE);
                    }
                } else {
                    errors.add("nickname", new ActionMessage("error.person.duplicate"));
                    saveErrors(request, errors);
                    logger.error("Duplicate person.");
                    return mapping.findForward(Constants.EDIT_PERSON_REDO);
                }
            } else {
                return mapping.findForward(Constants.EDIT_PERSON_REDO);
            }
        } else {
            return mapping.findForward(Constants.UNAUTHORIZED);
        }
    }

    /**
     * Prepares the form for deleting a person.
     * This is the erase abductor action called from the Struts framework.
     *
     * @param mapping       the ActionMapping used to select this instance
     * @param form          the optional ActionForm bean for this request
     * @param request       the HTTP Request we are processing
     * @param response      the HTTP Response we are processing
     * @return              the forwarding instance
     * @throws java.lang.Exception
     */
    public ActionForward erasePerson(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        User currentUser = null;
        if (request.getSession().getAttribute("currentuser") == null) {
            return mapping.findForward(Constants.EXPIRED);
        } else {
            currentUser = (User) request.getSession().getAttribute("currentuser");
        }
        if ((currentUser.getGroupId() == 0) || (currentUser.getGroupId() == 1)) {
            PersonForm personForm = (PersonForm) form;
            try {
                Person person = personService.getPersonById(personForm.getId());
                personForm.setFirstName(person.getFirstName());
                personForm.setNickname(person.getNickname());
                personForm.setLastName(person.getLastName());
                if (person.getAbductorId() != null) {
                    if (personService.countPersonsByAbductorId(person.getAbductorId()) == 1) {
                        personForm.setAbductorId(person.getAbductorId());
                        personForm.setAbductorFirstName(abductorService.getAbductorById(person.getAbductorId()).getFirstName());
                        personForm.setAbductorNickname(abductorService.getAbductorById(person.getAbductorId()).getNickname());
                        personForm.setAbductorLastName(abductorService.getAbductorById(person.getAbductorId()).getLastName());
                    }
                }
                if (person.getRelativeId() != null) {
                    if (personService.countPersonsByRelativeId(person.getRelativeId()) == 1) {
                        personForm.setRelativeId(person.getRelativeId());
                        personForm.setRelativeFirstName(relativeService.getRelativeById(person.getRelativeId()).getFirstName());
                        personForm.setRelativeLastName(relativeService.getRelativeById(person.getRelativeId()).getLastName());
                    }
                }
                personForm.setCode((int) (Math.random() * 7777) + 1000);
                if (currentUser.getGroupId() == 1) {
                    return mapping.findForward(Constants.DELETE_PERSON);
                } else {
                    return mapping.findForward(Constants.UNAUTHORIZED);
                }
            } catch (NumberFormatException nfe) {
                return mapping.findForward(Constants.LIST_PERSON);
            } catch (NullPointerException npe) {
                return mapping.findForward(Constants.LIST_PERSON);
            }
        } else {
            return mapping.findForward(Constants.UNAUTHORIZED);
        }
    }

    /**
     * Deletes a person from the database.
     * This is the delete person action called from the HTML form.
     *
     * @param mapping       the ActionMapping used to select this instance
     * @param form          the optional ActionForm bean for this request
     * @param request       the HTTP Request we are processing
     * @param response      the HTTP Response we are processing
     * @return              the forwarding instance
     * @throws java.lang.Exception
     */
    public ActionForward deletePerson(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        User currentUser = null;
        if (request.getSession().getAttribute("currentuser") == null) {
            return mapping.findForward(Constants.EXPIRED);
        } else {
            currentUser = (User) request.getSession().getAttribute("currentuser");
        }
        if ((currentUser.getGroupId() == 0) || (currentUser.getGroupId() == 1)) {
            PersonForm personForm = (PersonForm) form;
            try {
                Person person = personService.getPersonById(personForm.getId());
                if (personForm.getCode() == personForm.getUserCode()) {
                    if (currentUser.getGroupId() == 1) {
                        String absolutePhotoDirectory = getServlet().getServletContext().getRealPath("/") + "photo" + File.separator + createDirectoryName(person.getId());
                        File photoDirectory = new File(absolutePhotoDirectory);
                        if (photoDirectory.exists()) {
                            for (File primaryFile : photoDirectory.listFiles()) {
                                if (primaryFile.isDirectory()) {
                                    for (File secondaryFile : primaryFile.listFiles()) {
                                        secondaryFile.delete();
                                    }
                                }
                                primaryFile.delete();
                            }
                            photoDirectory.delete();
                        }
                        if (person.getRelativeId() != null) {
                            if (personService.countPersonsByRelativeId(person.getRelativeId()) == 1) {
                                relativeService.deleteRelative(person.getRelativeId());
                            }
                        }
                        if (person.getAbductorId() != null) {
                            if (personService.countPersonsByAbductorId(person.getAbductorId()) == 1) {
                                abductorService.deleteAbductor(person.getAbductorId());
                                absolutePhotoDirectory = getServlet().getServletContext().getRealPath("/") + "photo" + File.separator + "abductor-" + createDirectoryName(person.getAbductorId());
                                photoDirectory = new File(absolutePhotoDirectory);
                                if (photoDirectory.exists()) {
                                    for (File primaryFile : photoDirectory.listFiles()) {
                                        if (primaryFile.isDirectory()) {
                                            for (File secondaryFile : primaryFile.listFiles()) {
                                                secondaryFile.delete();
                                            }
                                        }
                                        primaryFile.delete();
                                    }
                                    photoDirectory.delete();
                                }
                            }
                        }
                        messageService.deleteMessagesForPerson(person.getId());
                        reportService.deleteReportsForPerson(person.getId());
                        personService.deletePerson(person.getId());
                        Log deleteLog = new Log();
                        deleteLog.setLog("Person " + person.getFirstName() + " \"" + person.getNickname() + "\" " + person.getLastName() + " was deleted by " + currentUser.getUsername() + ".");
                        deleteLog.setDate(simpleDateFormat.format(System.currentTimeMillis()));
                        logService.insertLog(deleteLog);
                        logger.info(deleteLog.toString());
                        request.setAttribute("person", person);
                        request.setAttribute("operation", "delete");
                        return mapping.findForward(Constants.DELETE_PERSON_SUCCESS);
                    } else {
                        return mapping.findForward(Constants.UNAUTHORIZED);
                    }
                } else {
                    personForm.setFirstName(person.getFirstName());
                    personForm.setNickname(person.getNickname());
                    personForm.setLastName(person.getLastName());
                    if (person.getAbductorId() != null) {
                        if (personService.countPersonsByAbductorId(person.getAbductorId()) == 1) {
                            personForm.setAbductorId(person.getAbductorId());
                            personForm.setAbductorFirstName(abductorService.getAbductorById(person.getAbductorId()).getFirstName());
                            personForm.setAbductorNickname(abductorService.getAbductorById(person.getAbductorId()).getNickname());
                            personForm.setAbductorLastName(abductorService.getAbductorById(person.getAbductorId()).getLastName());
                        }
                    }
                    if (person.getRelativeId() != null) {
                        if (personService.countPersonsByRelativeId(person.getRelativeId()) == 1) {
                            personForm.setRelativeId(person.getRelativeId());
                            personForm.setRelativeFirstName(relativeService.getRelativeById(person.getRelativeId()).getFirstName());
                            personForm.setRelativeLastName(relativeService.getRelativeById(person.getRelativeId()).getLastName());
                        }
                    }
                    personForm.setCode((int) (Math.random() * 7777) + 1000);
                    ActionMessages errors = new ActionMessages();
                    errors.add("usercode", new ActionMessage("error.code.mismatch"));
                    saveErrors(request, errors);
                    logger.error("Codes did not match.");
                    return mapping.findForward(Constants.DELETE_PERSON_REDO);
                }
            } catch (NullPointerException npe) {
                return mapping.findForward(Constants.LIST_PERSON);
            }
        } else {
            return mapping.findForward(Constants.UNAUTHORIZED);
        }
    }

    /**
     * Prints the person's poster in PDF file.
     *
     * @param mapping       the ActionMapping used to select this instance
     * @param form          the optional ActionForm bean for this request
     * @param request       the HTTP Request we are processing
     * @param response      the HTTP Response we are processing
     * @return              the forwarding instance
     * @throws java.lang.Exception
     */
    public ActionForward printPoster(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Document document = new Document(PageSize.LETTER, 50, 50, 50, 50);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Person person = (Person) personService.getPersonById(id);
            String tokens[] = person.getPhoto().split("\\/");
            String defaultPhotoBasename = "";
            for (int i = 0; i < tokens.length - 1; i++) {
                defaultPhotoBasename += tokens[i] + File.separator;
            }
            defaultPhotoBasename += tokens[tokens.length - 1];
            String absoluteDefaultPhotoFilename = getServlet().getServletContext().getRealPath("/") + defaultPhotoBasename;
            document.addTitle("Poster");
            document.addAuthor("OpenMPIS");
            document.addSubject("Poster for " + person.getNickname());
            document.addKeywords("OpenMPIS, missing, found, unidentified");
            document.addProducer();
            document.addCreationDate();
            document.addCreator("OpenMPIS version " + Constants.VERSION);
            document.open();
            if (person.getType() > 4) {
                Paragraph foundParagraph = new Paragraph("F O U N D", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 36, Font.BOLD, new Color(255, 0, 0)));
                foundParagraph.setAlignment(Paragraph.ALIGN_CENTER);
                document.add(foundParagraph);
            } else {
                Paragraph missingParagraph = new Paragraph("M I S S I N G", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 36, Font.BOLD, new Color(255, 0, 0)));
                missingParagraph.setAlignment(Paragraph.ALIGN_CENTER);
                document.add(missingParagraph);
            }
            Paragraph blackParagraph = new Paragraph(getResources(request).getMessage("month." + person.getMonthMissingOrFound()) + " " + person.getDayMissingOrFound() + ", " + person.getYearMissingOrFound(), FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD, new Color(0, 0, 0)));
            blackParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(blackParagraph);
            if (person.getType() < 5) {
                blackParagraph = new Paragraph(person.getMissingFromCity() + ", " + person.getMissingFromProvince(), FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD, new Color(0, 0, 0)));
                blackParagraph.setAlignment(Paragraph.ALIGN_CENTER);
                document.add(blackParagraph);
            }
            Paragraph redParagraph;
            if (!person.getNickname().isEmpty()) {
                redParagraph = new Paragraph(person.getFirstName() + " \"" + person.getNickname() + "\" " + person.getLastName(), FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, new Color(255, 0, 0)));
            } else {
                redParagraph = new Paragraph(person.getFirstName() + " " + person.getLastName(), FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, new Color(255, 0, 0)));
            }
            redParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(redParagraph);
            Image image = Image.getInstance(absoluteDefaultPhotoFilename);
            image.scaleAbsolute(200, 300);
            image.setAlignment(Image.ALIGN_CENTER);
            document.add(image);
            redParagraph = new Paragraph("Description", FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, new Color(255, 0, 0)));
            redParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(redParagraph);
            float[] widths = { 0.5f, 0.5f };
            PdfPTable pdfptable = new PdfPTable(widths);
            pdfptable.setWidthPercentage(100);
            if (person.getType() < 5) {
                pdfptable.addCell(new Phrase(getResources(request).getMessage("label.date.birth") + ": " + getResources(request).getMessage("month." + person.getBirthMonth()) + " " + person.getBirthDay() + ", " + person.getBirthYear(), FontFactory.getFont(FontFactory.HELVETICA, 12)));
                pdfptable.addCell(new Phrase(getResources(request).getMessage("label.address.city") + ": " + person.getCity(), FontFactory.getFont(FontFactory.HELVETICA, 12)));
            }
            pdfptable.addCell(new Phrase(getResources(request).getMessage("label.sex") + ": " + getResources(request).getMessage("sex." + person.getSex()), FontFactory.getFont(FontFactory.HELVETICA, 12)));
            pdfptable.addCell(new Phrase(getResources(request).getMessage("label.color.hair") + ": " + getResources(request).getMessage("color.hair." + person.getHairColor()), FontFactory.getFont(FontFactory.HELVETICA, 12)));
            pdfptable.addCell(new Phrase(getResources(request).getMessage("label.height") + ": " + person.getFeet() + "' " + person.getInches() + "\"", FontFactory.getFont(FontFactory.HELVETICA, 12)));
            pdfptable.addCell(new Phrase(getResources(request).getMessage("label.color.eye") + ": " + getResources(request).getMessage("color.eye." + person.getEyeColor()), FontFactory.getFont(FontFactory.HELVETICA, 12)));
            pdfptable.addCell(new Phrase(getResources(request).getMessage("label.weight") + ": " + person.getWeight() + " " + getResources(request).getMessage("label.weight.lbs"), FontFactory.getFont(FontFactory.HELVETICA, 12)));
            pdfptable.addCell(new Phrase(getResources(request).getMessage("label.race") + ": " + getResources(request).getMessage("race." + person.getRace()), FontFactory.getFont(FontFactory.HELVETICA, 12)));
            document.add(pdfptable);
            redParagraph = new Paragraph(getResources(request).getMessage("label.circumstance"), FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, new Color(255, 0, 0)));
            redParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(redParagraph);
            blackParagraph = new Paragraph(person.getCircumstance(), FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL));
            blackParagraph.setAlignment(Paragraph.ALIGN_JUSTIFIED);
            document.add(blackParagraph);
            blackParagraph = new Paragraph("------------------------------------------------------------------------------");
            blackParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(blackParagraph);
            blackParagraph = new Paragraph(getResources(request).getMessage("global.contact"), FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL));
            blackParagraph.setAlignment(Paragraph.ALIGN_JUSTIFIED);
            document.add(blackParagraph);
            document.close();
            response.setContentType("application/pdf");
            response.setContentLength(baos.size());
            response.setHeader("Content-disposition", "attachment; filename=Poster.pdf");
            baos.writeTo(response.getOutputStream());
            response.getOutputStream().flush();
            return null;
        } catch (NumberFormatException nfe) {
            return mapping.findForward(Constants.LIST_PERSON);
        } catch (NullPointerException npe) {
            return mapping.findForward(Constants.LIST_PERSON);
        }
    }

    /**
     * Validates the inputs from the person form.
     *
     * @param request       the HTTP Request we are processing
     * @param form          the ActionForm bean for this request
     * @return              <code>true</code> if there are no errors in the form; <code>false</code> otherwise
     */
    private boolean isValidPerson(HttpServletRequest request, ActionForm form) throws Exception {
        ActionMessages errors = new ActionMessages();
        Validator validator = new Validator();
        boolean isValid = true;
        Calendar calendar = Calendar.getInstance();
        PersonForm personForm = (PersonForm) form;
        FormFile photoFile = personForm.getPhotoFile();
        FormFile agedPhotoFile = personForm.getAgedPhotoFile();
        String firstName = personForm.getFirstName();
        String nickname = personForm.getNickname();
        String middleName = personForm.getMiddleName();
        String lastName = personForm.getLastName();
        int birthDay = personForm.getBirthDay();
        int birthMonth = personForm.getBirthMonth() - 1;
        int birthYear = personForm.getBirthYear();
        String street = personForm.getStreet();
        String city = personForm.getCity();
        String province = personForm.getProvince();
        double weight = personForm.getWeight();
        String marks = personForm.getMarks();
        String personalEffects = personForm.getPersonalEffects();
        int dayMissingOrFound = personForm.getDayMissingOrFound();
        int monthMissingOrFound = personForm.getMonthMissingOrFound() - 1;
        int yearMissingOrFound = personForm.getYearMissingOrFound();
        String missingFromCity = personForm.getMissingFromCity();
        String missingFromProvince = personForm.getMissingFromProvince();
        String possibleCity = personForm.getPossibleCity();
        String possibleProvince = personForm.getPossibleProvince();
        String circumstance = personForm.getCircumstance();
        String institution = personForm.getInstitution();
        String institutionStreet = personForm.getInstitutionStreet();
        String institutionCity = personForm.getInstitutionCity();
        String institutionProvince = personForm.getInstitutionProvince();
        String institutionEmail = personForm.getInstitutionEmail();
        String institutionNumber = personForm.getInstitutionNumber();
        String codisId = personForm.getCodisId();
        String afisId = personForm.getAfisId();
        String dentalId = personForm.getDentalId();
        Person existingPerson = new Person();
        if (personForm.getId() > 0) {
            existingPerson = personService.getPersonById(personForm.getId());
            if (existingPerson.getPhoto() != null) {
                personForm.setPhoto(existingPerson.getPhoto());
            }
            if (existingPerson.getAgedPhoto() != null) {
                personForm.setAgedPhoto(existingPerson.getAgedPhoto());
            }
        }
        if ((existingPerson.getPhoto() == null) || (existingPerson.getPhoto().equals("photo/unknown.png"))) {
            if (photoFile.getFileName().length() < 1) {
                errors.add("photofile", new ActionMessage("error.photo.required"));
            } else {
                if (!((photoFile.getContentType().equals("image/png")) || (photoFile.getContentType().equals("image/jpeg")) || (photoFile.getContentType().equals("image/gif")))) {
                    errors.add("photofile", new ActionMessage("error.photo.invalid"));
                }
            }
        }
        if ((agedPhotoFile.getFileName().length() > 1) && (!((agedPhotoFile.getContentType().equals("image/png")) || (agedPhotoFile.getContentType().equals("image/jpeg")) || (agedPhotoFile.getContentType().equals("image/gif"))))) {
            errors.add("agedphotofile", new ActionMessage("error.photo.invalid"));
        }
        if ((firstName.length() > 1) && (!validator.isValidFirstName(firstName))) {
            errors.add("firstname", new ActionMessage("error.firstname.invalid"));
        }
        if (firstName.length() < 1) {
            if (nickname.length() < 1) {
                errors.add("nickname", new ActionMessage("error.nickname.required"));
            } else {
                if (!validator.isValidFirstName(nickname)) {
                    errors.add("nickname", new ActionMessage("error.nickname.invalid"));
                }
            }
        }
        if ((middleName.length() > 1) && (!validator.isValidLastName(middleName))) {
            errors.add("middlename", new ActionMessage("error.middlename.invalid"));
        }
        if ((lastName.length() > 1) && (!validator.isValidLastName(lastName))) {
            errors.add("lastname", new ActionMessage("error.lastname.invalid"));
        }
        if (birthMonth > calendar.get(Calendar.MONTH) && (birthYear == calendar.get(Calendar.YEAR))) {
            errors.add("birthdate", new ActionMessage("error.birthmonth.invalid"));
        }
        if ((birthMonth == calendar.get(Calendar.MONTH)) && (birthDay > calendar.get(Calendar.DATE)) && (birthYear == calendar.get(Calendar.YEAR))) {
            errors.add("birthdate", new ActionMessage("error.birthday.invalid"));
        }
        if ((street.length() > 1) && (!validator.isValidStreet(street))) {
            errors.add("street", new ActionMessage("error.street.invalid"));
        }
        if ((city.length() > 1) && (!validator.isValidCity(city))) {
            errors.add("city", new ActionMessage("error.city.invalid"));
        }
        if ((province.length() > 1) && (!validator.isValidProvince(province))) {
            errors.add("province", new ActionMessage("error.province.invalid"));
        }
        if (weight < 1) {
            errors.add("weight", new ActionMessage("error.weight.required"));
        }
        if (marks.length() < 1) {
            errors.add("marks", new ActionMessage("error.marks.required"));
        }
        if (personalEffects.length() < 1) {
            errors.add("personaleffects", new ActionMessage("error.personaleffects.required"));
        }
        if ((monthMissingOrFound > calendar.get(Calendar.MONTH)) && (yearMissingOrFound == calendar.get(Calendar.YEAR))) {
            errors.add("datemissingorfound", new ActionMessage("error.monthmissingorfound.invalid"));
        }
        if ((monthMissingOrFound == calendar.get(Calendar.MONTH)) && (dayMissingOrFound > calendar.get(Calendar.DATE)) && (yearMissingOrFound == calendar.get(Calendar.YEAR))) {
            errors.add("datemissingorfound", new ActionMessage("error.daymissingorfound.invalid"));
        }
        if (missingFromCity.length() < 1) {
            errors.add("missingfromcity", new ActionMessage("error.city.required"));
        } else {
            if (!validator.isValidCity(missingFromCity)) {
                errors.add("missingfromcity", new ActionMessage("error.city.invalid"));
            }
        }
        if (missingFromProvince.length() < 1) {
            errors.add("missingfromprovince", new ActionMessage("error.province.required"));
        } else {
            if (!validator.isValidProvince(missingFromProvince)) {
                errors.add("missingfromprovince", new ActionMessage("error.province.invalid"));
            }
        }
        if ((possibleCity.length() > 1) && (!validator.isValidCity(possibleCity))) {
            errors.add("possiblecity", new ActionMessage("error.city.invalid"));
        }
        if ((possibleProvince.length() > 1) && (!validator.isValidProvince(possibleProvince))) {
            errors.add("possibleprovince", new ActionMessage("error.province.invalid"));
        }
        if (personForm.getType() < 5) {
            if (circumstance.length() < 1) {
                errors.add("circumstance", new ActionMessage("error.circumstance.required"));
            }
        }
        if (personForm.getType() > 4) {
            if (institution.length() < 1) {
                errors.add("institution", new ActionMessage("error.institution.required"));
            } else {
                if (!validator.isValidInstitution(institution)) {
                    errors.add("institution", new ActionMessage("error.institution.invalid"));
                }
            }
            if (institutionStreet.length() < 1) {
                errors.add("institutionstreet", new ActionMessage("error.street.required"));
            } else {
                if (!validator.isValidStreet(institutionStreet)) {
                    errors.add("institutionstreet", new ActionMessage("error.street.invalid"));
                }
            }
            if (institutionCity.length() < 1) {
                errors.add("institutioncity", new ActionMessage("error.city.required"));
            } else {
                if (!validator.isValidCity(institutionCity)) {
                    errors.add("institutioncity", new ActionMessage("error.city.invalid"));
                }
            }
            if (institutionProvince.length() < 1) {
                errors.add("institutionprovince", new ActionMessage("error.province.required"));
            } else {
                if (!validator.isValidProvince(institutionProvince)) {
                    errors.add("institutionprovince", new ActionMessage("error.province.invalid"));
                }
            }
            if ((institutionEmail.length() > 1) && (!validator.isValidEmailAddress(institutionEmail))) {
                errors.add("institutionemail", new ActionMessage("error.email.invalid"));
            }
            if (institutionNumber.length() < 1) {
                errors.add("institutionnumber", new ActionMessage("error.number.required"));
            } else {
                if (!validator.isValidNumber(institutionNumber)) {
                    errors.add("institutionnumber", new ActionMessage("error.number.invalid"));
                }
            }
        }
        if ((codisId.length() > 1) && (!validator.isValidId(codisId))) {
            errors.add("codisid", new ActionMessage("error.codisid.invalid"));
        }
        if ((afisId.length() > 1) && (!validator.isValidId(afisId))) {
            errors.add("afisid", new ActionMessage("error.afisid.invalid"));
        }
        if ((dentalId.length() > 1) && (!validator.isValidId(dentalId))) {
            errors.add("dentalid", new ActionMessage("error.dentalid.invalid"));
        }
        if (!errors.isEmpty()) {
            saveErrors(request, errors);
            isValid = false;
        }
        return isValid;
    }

    /**
     * Creates a unique directory name for the person's uploaded photos.
     * Adapted from http://snipplr.com/view/4321/generate-md5-hash-from-string/.
     *
     * @param id            the id of the person on which the directory name is based
     * @return              the 32 alphanumeric-equivalent of the nickname
     * @throws java.security.NoSuchAlgorithmException
     */
    private String createDirectoryName(Integer id) throws NoSuchAlgorithmException {
        StringBuffer uniqueDirectoryName = new StringBuffer();
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.reset();
        md5.update(id.byteValue());
        byte digest[] = md5.digest();
        for (int i = 0; i < digest.length; i++) {
            uniqueDirectoryName.append(Integer.toHexString(0xFF & digest[i]));
        }
        return uniqueDirectoryName.toString();
    }

    /**
     * Calculates the age of a person.
     * Adapted from http://www.coderanch.com/t/391834/Java-General-beginner/java/there-better-way-calculate-age
     *
     * @param birthMonth    the person's birth month
     * @param birthDay      the person's birth day
     * @param birthYear     the person's birth year
     * @return              the person's age
     */
    private int getAge(int birthMonth, int birthDay, int birthYear) {
        Calendar birthDate = Calendar.getInstance();
        birthDate.set(birthYear, birthMonth, birthDay);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }

    /**
     * Calculates the number of days missing.
     *
     * @param birthMonth    the person's birth month
     * @param birthDay      the person's birth day
     * @param birthYear     the person's birth year
     * @return              the number of days missing
     */
    private int getDaysMissing(int birthMonth, int birthDay, int birthYear) {
        Calendar birthDate = Calendar.getInstance();
        birthDate.set(birthYear, birthMonth, birthDay);
        Calendar today = Calendar.getInstance();
        long ms = today.getTimeInMillis() - birthDate.getTimeInMillis();
        long days = ms / (1000 * 60 * 60 * 24);
        return Long.valueOf(days).intValue();
    }
}
