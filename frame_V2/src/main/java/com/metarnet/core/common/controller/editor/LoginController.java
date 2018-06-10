package com.metarnet.core.common.controller.editor;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-2-27
 * Time: 上午11:40
 * To change this template use File | Settings | File Templates.
 */

import com.alibaba.fastjson.JSON;
import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.controller.BaseController;
import com.metarnet.core.common.exception.UIException;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController extends BaseController {
	@RequestMapping(value = "/loginController.do", params = "method=userlogin")
	public void userlogin(HttpServletRequest request,HttpServletResponse response,String username,String password) throws UIException, PaasAAAAException {
//		UserEntity userEntity = AAAAAdapter.getInstence()
//				.login(username, password);
        UserEntity userEntity = AAAAAdapter.getInstence().login(username , password);
//		request.getSession().setAttribute(globalUniqueID, userEntity.getUserId());
		request.getSession().setAttribute(globalUniqueUser, userEntity);
		request.getSession().setAttribute(globalUniqueID, userEntity.getAttribute1());
		request.getSession().setAttribute("userRangeRootDeptId",
				super.getUserRangeRootDeptId(Integer.valueOf(userEntity.getOrgID().toString())));
        try {
            response.sendRedirect("menu.jsp");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

	/**
	 * 在平台上，由于第一次进入jsp无法获取用户session，因此打开第一个jsp时，需要向后台请求一下，从而初始化用户session
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws UIException
	 * @throws com.ucloud.paas.proxy.aaaa.util.PaasAAAAException
	 */
	@RequestMapping(value = "/loginController.do", params = "method=getUserSessioin")
	@ResponseBody
	public void getUserSessioin(HttpServletRequest request,
			HttpServletResponse response) throws UIException, PaasAAAAException {
		try {
			Map map = new HashMap();
			UserEntity currentUser = this.getUserEntity(request);
			map.put("cUserDeptId", currentUser.getOrgID());
			map.put("cUserDeptName", AAAAAdapter.getInstence().findOrgByOrgID(
					currentUser.getOrgID()).getOrgName());
			map.put("cUserId", currentUser.getUserId());
			map.put("cUserName", currentUser.getTrueName());
			map.put("cUserRangeRootDeptId", super
					.getUserRangeRootDeptId(Integer.valueOf(currentUser.getOrgID().toString())));
			this.endHandle(request, response, JSON.toJSONString(map) , "" , false);
		} catch (Exception e) {
			e.printStackTrace();
//			throw new UIException(null, "用户session获取失败");
		}
	}
}
