package com.boot.smartrelay.controller;


import com.boot.smartrelay.beans.AdminDeviceModel;
import com.boot.smartrelay.beans.AdminUser;
import com.boot.smartrelay.beans.ResponseBox;
import com.boot.smartrelay.beans.User;
import com.boot.smartrelay.service.AdminService;
import com.boot.smartrelay.service.UserService;
import com.boot.smartrelay.validator.AdminDeviceModelValidator;
import com.boot.smartrelay.validator.AdminValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

   @Resource(name = "loginAdminUser")
   private AdminUser loginAdminUser;

   final AdminValidator adminValidator;

   final AdminDeviceModelValidator adminDeviceModelValidator;

   final AdminService adminService;

   final UserService userService;


    @GetMapping("/login")
    public String login(HttpServletRequest request, @ModelAttribute("adminUser") AdminUser user, @RequestParam(value="fail", defaultValue = "false") boolean fail){
        if(loginAdminUser.isLogin()){
            return "login_try";
        }

        if(fail == true){
            request.setAttribute("fail", "true");
        }
        return "login";
    }

    @PostMapping("/login_pro")
    public String login_pro(HttpServletRequest request, @ModelAttribute("adminUser") @Valid AdminUser user, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            request.setAttribute("reason", "아이디 혹은 비밀번호가 틀립니다.");
            return "login_fail";
        }
        loginAdminUser.setLogin(true);
        return "login_success";
    }

    @GetMapping("/logout")
    public String logout(){
        loginAdminUser.setLogin(false);
        return "logout";
    }

    @GetMapping("/not_login")
    public String not_login(){
        return "not_login";
    }

    @GetMapping("/main")
    public String main(HttpServletRequest request){
        request.setAttribute("adminUser", loginAdminUser);
        return "main";
    }

    @PostMapping("/main_pro")
    public String main_pro(ModelMap model, @Valid @ModelAttribute("adminDeviceModel") AdminDeviceModel adminDeviceModel, BindingResult bindingResult){

        if(bindingResult.hasErrors()){
            model.addAttribute("message","아이디, 비밀번호, 디바이스 정보가 모두 기입되어 있어야 합니다. / 중복된 아이디가 있는지도 조심하십시오");
            return "main_fail";
        }

        //1. User 파라미터 생성
        User user = User.builder()
                    .loginFlg(false)
                    .password(adminDeviceModel.getUserPw())
                    .id(adminDeviceModel.getUserId())
                    .largeSector(new ArrayList<>())
                    .smallSector(new ArrayList<>())
                    .deviceIdList(new ArrayList<>())
                    .build();
         
       ResponseBox smartRelaySaved = adminService.saveSmartRelayDeviceIds(adminDeviceModel.getDeviceId());
       
       if(!smartRelaySaved.isStatus()) {
    	   model.addAttribute("message", smartRelaySaved.getMessage());
       }
       
        ResponseBox userBox = adminService.createUser(user);
        if(!userBox.isStatus()){
            model.addAttribute("message", userBox.getMessage());
            return "main_fail";
        }
        //어드민 디바이스 맵핑 시전
        if(userBox.isStatus()){
            ResponseBox adminBox = adminService.addDeviceToUser(user.getId(), adminDeviceModel.getDeviceId());
            if(!adminBox.isStatus()){
                model.addAttribute("message", adminBox.getMessage());
                return "main_fail";
            }
        }
        model.addAttribute("message", "성공적으로 등록되었습니다.");
        return "main_success";
    }

    @PostMapping("/main_add_pro")
    public String main_add_pro(ModelMap model, @Valid @ModelAttribute("adminDeviceModel") AdminDeviceModel adminDeviceModel, BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            model.addAttribute("message","아이디, 비밀번호, 디바이스 정보가 모두 기입되어 있어야 합니다.");
            return "main_fail";
        }


        ResponseBox adminBox = adminService.addDeviceToUser(adminDeviceModel.getUserId(), adminDeviceModel.getDeviceId());
        if(!adminBox.isStatus()){
            model.addAttribute("message", adminBox.getMessage());
            return "main_fail";
        }
        model.addAttribute("message", "성공적으로 등록되었습니다.");
        return "main_success";
    }

    @PostMapping("/main_del_pro")
    public String main_del_pro(ModelMap model, @Valid @ModelAttribute("adminDeviceModel") AdminDeviceModel adminDeviceModel, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            model.addAttribute("message","아이디, 비밀번호, 디바이스 정보가 모두 기입되어 있어야 합니다.");
            return "main_fail";
        }
        ResponseBox responseBox = adminService.deleteDeviceFromUser(adminDeviceModel.getUserId(), adminDeviceModel.getDeviceId());
        model.addAttribute("message", responseBox.getMessage());
        if(!responseBox.isStatus()){
            return "main_fail";
        }
        return "main_success";

    }

    @InitBinder("adminUser")
    public void initBinder1(WebDataBinder webDataBinder){
        webDataBinder.addValidators(adminValidator);
    }

    @InitBinder("adminDeviceModel")
    public void initBinder2(WebDataBinder webDataBinder){
        webDataBinder.addValidators(adminDeviceModelValidator);
    }

}
