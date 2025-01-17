/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import datamodel.rest.MountModuleReq;
import datamodel.rest.UpdateModule;
import datamodel.rest.CheckUserLogin;
import datamodel.rest.ErrorRsp;
import datamodel.rest.GetModuleRsp;
import datamodel.rest.GetTutorialRsp;
import datamodel.rest.GetUserRsp;
import datamodel.rest.GetVenueRsp;
import datamodel.rest.MountTutorial;
import datamodel.rest.UpdateModuleTutorial;
import datamodel.rest.UpdateTutorialStudent;
import ejb.DataInitSessionBean;
import entities.Feedback;
import entities.Module;
import entities.Tutorial;
import entities.User;
import entities.Venue;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static util.AccessRightEnum.Admin;

/**
 *
 * @author Vixson
 */
@Stateless
@Path("ModuleMounting")
public class ModuleMountingResource {

    @PersistenceContext(unitName = "LMS-warPU")
    private EntityManager em;

    public ModuleMountingResource() {
    }

    public boolean isLogin(User user) {
        user = em.find(User.class, user.getId());
        if (user != null) {
            return true;
        }
        return false;
    }

    public User userLogin(String username, String password) {
        User user = em.find(User.class, username);
        if (user != null) {
            if (user.getPassword().equals(password)) {
                return user;
            }
        }
        throw new NotFoundException("Username does not exist");
    }

    /*module.setAnnoucementList(null);
                module.setAttandanceList(null);
                module.setClassGroupList(null);
                module.setConsultationList(null);
                module.setFeedback(null);
                module.setFolderList(null);
                module.setForumPostList(null);
                module.setGrade(null);
                module.setGradeItemList(null);
                module.setLessonPlanList(null);
                module.setPublicUserList(null);
                module.setQuizList(null);
                module.setStudentList(null);
                module.setFeedbackList(null)*/
    @PUT
    @Path(value = "mountModule")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response mountModule(MountModuleReq mountModuleReq, @QueryParam("userId") Long userId, @QueryParam("venueId") Long venueId) {
        try {
            Module module = new Module();
            module.setCode(mountModuleReq.getCode());
            module.setTitle(mountModuleReq.getTitle());
            module.setDescription(mountModuleReq.getDescription());
            module.setSemesterOffered(mountModuleReq.getSemesterOffered());
            module.setYearOffered(mountModuleReq.getYearOffered());
            module.setCreditUnit(mountModuleReq.getCreditUnit());
            module.setMaxEnrollment(mountModuleReq.getMaxEnrollment());
            module.setHasExam(mountModuleReq.isHasExam());
            module.setExamTime(mountModuleReq.getExamTime());

            Venue venue = em.find(Venue.class, venueId);
            Venue venueCopy = new Venue(venue.getId(), venue.getName());
            module.setExamVenue(venue);
            User user = em.find(User.class, userId);
            User userCopy = new User(user.getFirstName(), user.getLastName(), user.getEmail(),
                    user.getUsername(), null, user.getGender(), null, null, null, null, null, null, null, null);
            module.setAssignedTeacher(user);
            module.setLectureDetails(mountModuleReq.getLectureDetails());
            module.setFaculty(mountModuleReq.getFaculty());
            module.setDepartment(mountModuleReq.getDepartment());
            em.persist(module);
            DataInitSessionBean dataInit = new DataInitSessionBean();
            dataInit.createSurvey(module);
            em.flush();
            Module moduleCopy = new Module(module.getModuleId(), module.getCode(), module.getTitle(), module.getDescription(),
                    module.getSemesterOffered(), module.getYearOffered(), module.getCreditUnit(), null, module.getMaxEnrollment(), null,
                    null, null, null, null, null, null, null, null, null, userCopy, null, null, null, module.isHasExam(), module.getExamTime(),
                    venueCopy, module.getLectureDetails(), module.getDepartment(), module.getFaculty());
            return Response.status(Response.Status.OK).entity(moduleCopy).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path(value = "mountTutorial")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response mountTutorial(MountTutorial mountTutorial, @QueryParam("moduleId") Long moduleId, @QueryParam("venueId") Long venueId) {
        try {
            Module module = em.find(Module.class, moduleId);
            if (module == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Module does not exist").build();
            }
            Venue venue = em.find(Venue.class, venueId);
            if (venue == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Venue does not exist").build();
            }
            Tutorial tutorial = new Tutorial();
            tutorial.setMaxEnrollment(mountTutorial.getMaxEnrollment());
            tutorial.setVenue(venue);
            tutorial.setTiming(mountTutorial.getTiming());;
            tutorial.setModule(module);
            em.persist(tutorial);
            em.flush();
            module.getTutorials().add(tutorial);
            Venue venueCopy = new Venue(venue.getId(), venue.getName());
            Tutorial tutorialCopy = new Tutorial(tutorial.getTutorialId(), tutorial.getMaxEnrollment(),
                    venueCopy, tutorial.getTiming(), null, null);
            return Response.status(Response.Status.OK).entity(tutorialCopy).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path(value = "getModule/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getModuleById(@PathParam("id") Long moduleId) {
        try {
            Module module = em.find(Module.class, moduleId);

            if (module == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorRsp("Module does not exist")).build();
            }

            User teacher = module.getAssignedTeacher();
            User teacherCopy = new User(null, teacher.getId(), teacher.getFirstName(), teacher.getLastName(), teacher.getEmail(),
                    teacher.getUsername(), null, teacher.getGender(), teacher.getAccessRight(),
                    null, null, null, null, null, null, null);

            List<Tutorial> tutorialsCopy = new ArrayList<>();
            List<Tutorial> tutorials = module.getTutorials();
            for (Tutorial t : tutorials) {
                t.getMaxEnrollment();
                t.getVenue();
                t.getTiming();
                t.getStudentList();
                t.getModule();
                tutorialsCopy.add(new Tutorial(
                        t.getTutorialId(), t.getMaxEnrollment(), t.getVenue(),
                        t.getTiming(), null, null));
            }

            List<Feedback> feedbackListCopy = new ArrayList<>();
            List<Feedback> feedbackList = module.getFeedbackList();
            for (Feedback f : feedbackList) {
                f.getCreateTs();
                f.getFeedback();
                feedbackListCopy.add(new Feedback(
                        f.getFeedbackId(), f.getFeedback(), f.getCreateTs()));
            }

            Module moduleCopy = new Module(module.getModuleId(), module.getCode(), module.getTitle(),
                    module.getDescription(), module.getSemesterOffered(),
                    module.getYearOffered(), module.getCreditUnit(), null, module.getMaxEnrollment(),
                    null, null, null, null, null, null, null, null, null, null,
                    teacherCopy, null, feedbackListCopy, tutorialsCopy, module.isHasExam(),
                    module.getExamTime(), module.getExamVenue(), module.getLectureDetails(), module.getFaculty(),
                    module.getDepartment());

            return Response.status(Response.Status.OK).entity(moduleCopy).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorRsp(ex.getMessage())).build();
        }
    }

    @Path(value = "getModuleByCode/{code}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getModuleByCode(@PathParam("code") String code) {
        try {
            Query query = em.createQuery("select m from Module m where m.code = :code");
            query.setParameter("code", code);
            Module module = (Module) query.getSingleResult();

            if (module == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Module does not exist").build();
            }

            User teacher = module.getAssignedTeacher();
            User teacherCopy = new User(null, teacher.getId(), teacher.getFirstName(), teacher.getLastName(), teacher.getEmail(),
                    teacher.getUsername(), null, teacher.getGender(), teacher.getAccessRight(),
                    null, null, null, null, null, null, null);

            List<Tutorial> tutorialsCopy = new ArrayList<>();
            List<Tutorial> tutorials = module.getTutorials();
            for (Tutorial t : tutorials) {
                t.getMaxEnrollment();
                t.getVenue();
                t.getTiming();
                t.getStudentList();
                t.getModule();
                tutorialsCopy.add(new Tutorial(
                        t.getTutorialId(), t.getMaxEnrollment(), t.getVenue(),
                        t.getTiming(), null, null));
            }

            List<Feedback> feedbackListCopy = new ArrayList<>();
            List<Feedback> feedbackList = module.getFeedbackList();
            for (Feedback f : feedbackList) {
                f.getCreateTs();
                f.getFeedback();
                feedbackListCopy.add(new Feedback(
                        f.getFeedbackId(), f.getFeedback(), f.getCreateTs()));
            }

            Module moduleCopy = new Module(module.getModuleId(), module.getCode(), module.getTitle(),
                    module.getDescription(), module.getSemesterOffered(),
                    module.getYearOffered(), module.getCreditUnit(), null, module.getMaxEnrollment(),
                    null, null, null, null, null, null, null, null, null, null,
                    teacherCopy, null, feedbackListCopy, tutorialsCopy, module.isHasExam(),
                    module.getExamTime(), module.getExamVenue(), module.getLectureDetails(), module.getFaculty(),
                    module.getDepartment());

            return Response.status(Response.Status.OK).entity(moduleCopy).build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path(value = "getAllModule")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllModule() {

        try {
            Query query = em.createQuery("select m from Module m");
            List<Module> moduleList = query.getResultList();

            GetModuleRsp rsp = new GetModuleRsp();
            rsp.setModule(new ArrayList<>());
            if (moduleList == null && moduleList.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("No module found").build();
            } else {
                for (Module module : moduleList) {
                    User teacher = module.getAssignedTeacher();
                    User teacherCopy = new User(null, teacher.getId(), teacher.getFirstName(), teacher.getLastName(), teacher.getEmail(),
                            teacher.getUsername(), null, teacher.getGender(), teacher.getAccessRight(),
                            null, null, null, null, null, null, null);
                    Venue venue = module.getExamVenue();
                    Venue venueCopy = new Venue(venue.getId(), venue.getName());
                    List<Tutorial> tutorialsCopy = new ArrayList<>();
                    List<Tutorial> tutorials = module.getTutorials();
                    for (Tutorial t : tutorials) {
                        Venue tutVenue = t.getVenue();
                        Venue tutVenueCopy = new Venue(tutVenue.getId(), tutVenue.getName());
                        t.getMaxEnrollment();
                        t.getTiming();
                        t.getStudentList();
                        t.getModule();
                        tutorialsCopy.add(new Tutorial(
                                t.getTutorialId(), t.getMaxEnrollment(), tutVenueCopy,
                                t.getTiming(), null, null));
                    }
                    List<Feedback> feedbackListCopy = new ArrayList<>();
                    List<Feedback> feedbackList = module.getFeedbackList();
                    for (Feedback f : feedbackList) {
                        f.getCreateTs();
                        f.getFeedback();
                        feedbackListCopy.add(new Feedback(
                                f.getFeedbackId(), f.getFeedback(), f.getCreateTs()));
                    }
                    rsp.getModule().add(
                            new Module(module.getModuleId(), module.getCode(), module.getTitle(),
                                    module.getDescription(), module.getSemesterOffered(),
                                    module.getYearOffered(), module.getCreditUnit(), null, module.getMaxEnrollment(),
                                    null, null, null, null, null, null, null, null, null, null,
                                    teacherCopy, null, feedbackListCopy, tutorialsCopy, module.isHasExam(),
                                    module.getExamTime(), venueCopy, module.getLectureDetails(), module.getFaculty(),
                                    module.getDepartment()));
                }
                return Response.status(Response.Status.OK).entity(rsp).build();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path(value = "deleteModule")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteModule(@QueryParam("moduleId") Long moduleId) {

        //if (checkUserLogin.getUser().getAccessRight() == Admin) {
        try {
            Module module = em.find(Module.class, moduleId);
            if (module == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("No module found").build();

            }
            em.remove(module);

            return Response.status(Response.Status.OK).entity("Module deleted").build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        //}
        //return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @Path(value = "deleteModuleByCode")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteModuleByCode(@QueryParam("code") String code, CheckUserLogin checkUserLogin) {

        //if (isLogin(checkUserLogin.getUser()) == true && checkUserLogin.getUser().getAccessRight() == Admin) {
        try {
            Query query = em.createQuery("select m from Module m where m.code = :code");
            query.setParameter("code", code);
            Module module = (Module) query.getSingleResult();

            if (module != null) {
                em.remove(module);
                return Response.status(Response.Status.OK).entity(module).build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity("No module found").build();

        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        //}
        //return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @Path(value = "updateModule")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateModule(UpdateModule updateModule, @QueryParam("moduleId") Long moduleId, @QueryParam("userId") Long userId, @QueryParam("venueId") Long venueId) {

        try {

            Module module = em.find(Module.class, moduleId);

            if (module != null) {
                module.setCode(updateModule.getCode());
                module.setTitle(updateModule.getTitle());
                module.setDescription(updateModule.getDescription());
                module.setSemesterOffered(updateModule.getSemesterOffered());
                module.setYearOffered(updateModule.getYearOffered());
                module.setCreditUnit(updateModule.getCreditUnit());
                module.setMaxEnrollment(updateModule.getMaxEnrollment());
                module.setHasExam(updateModule.isHasExam());
                module.setExamTime(updateModule.getExamTime());
                Venue venue = em.find(Venue.class, venueId);
                Venue venueCopy = new Venue(venue.getId(), venue.getName());
                module.setExamVenue(venue);
                User user = em.find(User.class, userId);
                User userCopy = new User(user.getFirstName(), user.getLastName(), user.getEmail(),
                        user.getUsername(), null, user.getGender(), null, null, null, null, null, null, null, null);
                module.setAssignedTeacher(user);
                module.setLectureDetails(updateModule.getLectureDetails());
                module.setFaculty(updateModule.getFaculty());
                module.setDepartment(updateModule.getDepartment());
                em.merge(module);
                em.flush();
                Module moduleCopy = new Module(module.getModuleId(), module.getCode(), module.getTitle(), module.getDescription(),
                        module.getSemesterOffered(), module.getYearOffered(), module.getCreditUnit(), null, module.getMaxEnrollment(), null,
                        null, null, null, null, null, null, null, null, null, userCopy, null, null, null, module.isHasExam(), module.getExamTime(),
                        venueCopy, module.getLectureDetails(), module.getDepartment(), module.getFaculty());
                return Response.status(Response.Status.OK).entity(moduleCopy).build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity("Module does not exist").build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /*@Path(value = "updateModuleWithTutorial")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateModuleWithTutorial(UpdateModule updateModule, @QueryParam("moduleId") Long moduleId, @QueryParam("userId") Long userId, @QueryParam("venueId") Long venueId) {

        try {

            Module module = em.find(Module.class, moduleId);
            Venue venue = em.find(Venue.class, venueId);
            if (venue == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Venue does not exist").build();
            }

            if (module != null) {
                module.setCode(updateModule.getCode());
                module.setTitle(updateModule.getTitle());
                module.setDescription(updateModule.getDescription());
                module.setSemesterOffered(updateModule.getSemesterOffered());
                module.setYearOffered(updateModule.getYearOffered());
                module.setCreditUnit(updateModule.getCreditUnit());
                module.setMaxEnrollment(updateModule.getMaxEnrollment());
                User user = em.find(User.class, userId);
                User userCopy = new User(user.getFirstName(), user.getLastName(), user.getEmail(),
                        user.getUsername(), null, user.getGender(), null, null, null, null, null, null, null, null);
                module.setAssignedTeacher(user);
                module.setLectureDetails(updateModule.getLectureDetails());
                module.setFaculty(updateModule.getFaculty());
                module.setDepartment(updateModule.getDepartment());
                if (module.getTutorials().isEmpty()) {
                    em.merge(module);
                    return Response.status(Response.Status.NOT_FOUND).entity("Module has no tutorial").build();
                }
                List<Tutorial> tutorialsCopy = new ArrayList<>();
                List<Tutorial> tutorials = module.getTutorials();
                for (Tutorial t : tutorials) {
                    t.setMaxEnrollment(updateModule.getMaxEnrollment());
                    t.setVenue(venue);
                    t.setTiming(updateModule.getTiming());
                    em.merge(t);
                    tutorialsCopy.add(new Tutorial(t.getTutorialId(), t.getMaxEnrollment(),
                            t.getVenue(), t.getTiming(), null, null));
                }
                Module moduleCopy = new Module(module.getModuleId(), module.getCode(), module.getTitle(), module.getDescription(),
                        module.getSemesterOffered(), module.getYearOffered(), module.getCreditUnit(), null, module.getMaxEnrollment(), null,
                        null, null, null, null, null, null, null, null, null, userCopy, null, null, null, module.isHasExam(), module.getExamTime(),
                        module.getExamVenue(), module.getLectureDetails(), module.getDepartment(), module.getFaculty());
                return Response.status(Response.Status.OK).entity(moduleCopy).build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity("Module does not exist").build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }*/
    @Path(value = "updateModuleDescription/{moduleId}")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateModuleDescription(String description, @PathParam("moduleId") Long moduleId) {

        try {

            Module module = em.find(Module.class, moduleId);

            if (module == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorRsp("Module does not exist")).build();
            }

            module.setDescription(description);

//            em.merge(module);
            em.flush();

            return Response.status(Response.Status.OK).build();

        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorRsp(ex.getMessage())).build();
        }
    }

    @Path(value = "updateModuleTutorial")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateModuleTutorial(UpdateModuleTutorial updateModuleTutorial, @QueryParam("moduleId") Long moduleId, @QueryParam("venueId") Long venueId) {

        try {
            Module module = em.find(Module.class, moduleId);
            if (module == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Module does not exist").build();
            }
            if (module.getTutorials().isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("Module has no tutorial").build();
            }
            Venue venue = em.find(Venue.class, venueId);
            if (venue == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Venue does not exist").build();
            }
            Venue venueCopy = new Venue(venue.getId(), venue.getName());
            List<Tutorial> tutorialsCopy = new ArrayList<>();
            List<Tutorial> tutorials = module.getTutorials();
            for (Tutorial t : tutorials) {
                t.setMaxEnrollment(updateModuleTutorial.getMaxEnrollment());
                t.setVenue(venue);
                t.setTiming(updateModuleTutorial.getTiming());
                em.merge(t);
                em.flush();
                tutorialsCopy.add(new Tutorial(
                        t.getTutorialId(), t.getMaxEnrollment(), venueCopy,
                        t.getTiming(), null, null));
            }
            return Response.status(Response.Status.OK).entity(tutorialsCopy).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path(value = "updateTutorial")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTutorial(UpdateModuleTutorial updateModuleTutorial, @QueryParam("tutorialId") Long tutorialId, @QueryParam("venueId") Long venueId) {

        try {
            Tutorial tutorial = em.find(Tutorial.class, tutorialId);
            if (tutorial == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Tutorial does not exist").build();
            }
            Venue venue = em.find(Venue.class, venueId);
            if (venue == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Venue does not exist").build();
            }
            tutorial.setMaxEnrollment(updateModuleTutorial.getMaxEnrollment());
            tutorial.setVenue(venue);
            tutorial.setTiming(updateModuleTutorial.getTiming());
            em.merge(tutorial);
            em.flush();
            Venue venueCopy = new Venue(venue.getId(), venue.getName());
            Tutorial tutorialCopy = new Tutorial(tutorial.getTutorialId(), tutorial.getMaxEnrollment(),
                    venueCopy, tutorial.getTiming(), null, null);
            return Response.status(Response.Status.OK).entity(tutorialCopy).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path(value = "updateTutorialWithStudent")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTutorialWithStudent(UpdateTutorialStudent updateTutorialStudent, @QueryParam("tutorialId") Long tutorialId, @QueryParam("venueId") Long venueId) {

        try {
            Tutorial tutorial = em.find(Tutorial.class, tutorialId);
            if (tutorial == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Tutorial does not exist").build();
            }
            Venue venue = em.find(Venue.class, venueId);
            if (venue == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Venue does not exist").build();
            }
            tutorial.setMaxEnrollment(updateTutorialStudent.getMaxEnrollment());
            tutorial.setVenue(venue);
            tutorial.setTiming(updateTutorialStudent.getTiming());
            List<User> studentsCopy = new ArrayList<>();
            List<User> students = tutorial.getStudentList();
            for (User s : students) {
                s.setId(updateTutorialStudent.getUserId());
                s.setEmail(updateTutorialStudent.getEmail());
                s.setFirstName(updateTutorialStudent.getFirstName());
                s.setLastName(updateTutorialStudent.getLastName());
                s.setGender(updateTutorialStudent.getGender());
                s.setUsername(updateTutorialStudent.getUsername());
                studentsCopy.add(new User(s.getFirstName(), s.getLastName(), s.getEmail(), s.getUsername(), null,
                        s.getGender(), null, null, null, null, null, null, null, null));
            }
            em.merge(tutorial);
            em.flush();
            Venue venueCopy = new Venue(venue.getId(), venue.getName());
            Tutorial tutorialCopy = new Tutorial(tutorial.getTutorialId(), tutorial.getMaxEnrollment(),
                    venueCopy, tutorial.getTiming(), studentsCopy, null);
            return Response.status(Response.Status.OK).entity(tutorialCopy).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path(value = "getAllTutorialByModule")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTutorialByModule(@QueryParam("moduleId") Long moduleId) {
        try {
            Module module = em.find(Module.class, moduleId);
            if (module == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorRsp("No module found")).build();
            }
            /*Query query = em.createQuery("select t from Tutorial t where t.module = :moduleId");
            query.setParameter("moduleId", moduleId);
            List<Tutorial> tutorials = query.getResultList();*/
            GetTutorialRsp rsp = new GetTutorialRsp(new ArrayList<>(), new ArrayList<>());
            List<Tutorial> tutorials = module.getTutorials();
            if (tutorials == null && tutorials.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorRsp("No tutorial found")).build();
            } else {
                for (Tutorial tutorial : tutorials) {
                    Tutorial temp = em.find(Tutorial.class, tutorial.getTutorialId());
                    int currentEnrollment = temp.getStudentList().size();
                    Venue v = tutorial.getVenue();
                    Venue vCopy = new Venue(v.getId(), v.getName());
                    /*List<User> students = tutorial.getStudentList();
                    for (User s : students) {
                        /*User user = em.find(User.class, userId);
                        if (tutorial.getStudentList().contains(user)) {
                        s.getAccessRight();
                        //s.getClassGroupList();
                        //s.getConsultationTimeslotList();
                        s.getEmail();
                        s.getFirstName();
                        s.getGender();
                        s.getLastName();
                        //s.getQuizAttemptList();
                        //s.getStudentModuleList();
                        //s.getSurveyAttemptList();
                        //s.getTutorials();
                        s.getUsername();
                        students.add(s);
                    }*/
                    //}
                    rsp.getTutorials().add(
                            new Tutorial(tutorial.getTutorialId(), tutorial.getMaxEnrollment(),
                                    vCopy, tutorial.getTiming(), null, null));
                    rsp.getCurrentEnrollment().add(new Integer(currentEnrollment));
                }
            }
            return Response.status(Response.Status.OK).entity(rsp).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorRsp(ex.getMessage())).build();
        }
    }

    @GET
    @Path(value = "getAllStudentByModule")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllStudentByModule(@QueryParam("moduleId") Long moduleId) {
        try {
            Module module = em.find(Module.class, moduleId);
            if (module == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorRsp("No module found")).build();
            }
            GetUserRsp rsp = new GetUserRsp(new ArrayList<>());
            List<User> students = module.getStudentList();
            if (students == null && students.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorRsp("No students found")).build();
            }
            for (User s : students) {
                rsp.getUserList().add(
                        new User(null, s.getUserId(), s.getFirstName(),
                                s.getLastName(), s.getEmail(), s.getUsername(), null,
                                s.getGender(), s.getAccessRight(), null,
                                null, null,
                                null, null, null, null));

            }
            return Response.status(Response.Status.OK).entity(rsp).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorRsp(ex.getMessage())).build();
        }
    }

    @Path(value = "deleteTutorial")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTutorial(@QueryParam("moduleId") Long moduleId, @QueryParam("tutorialId") Long tutorialId) {
        try {
            Module module = em.find(Module.class, moduleId);
            Tutorial tutorial = em.find(Tutorial.class, tutorialId);
            /*if(!module.getTutorials().contains(tutorial)){
                 return Response.status(Response.Status.NOT_FOUND).entity("No tutorial found in module").build();
            }*/
            if (tutorial == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("No tutorial found").build();
            }
            em.remove(tutorial);
            module.getTutorials().remove(tutorial);
            return Response.status(Response.Status.OK).entity("Tutorial deleted").build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path(value = "getAllVenue")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllVenue() {
        try {
            Query query = em.createQuery("select v from Venue v");
            List<Venue> venueList = query.getResultList();
            if (venueList == null && venueList.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("No venue found").build();
            }
            GetVenueRsp rsp = new GetVenueRsp(new ArrayList<>());
            for (Venue v : venueList) {
                rsp.getVenueList().add(new Venue(
                        v.getId(), v.getName()));
            }
            return Response.status(Response.Status.OK).entity(rsp).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
