package com.javaex.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


import com.javaex.model.NoticeDao;
import com.javaex.model.ReservationDao;
import com.javaex.model.ReservationVo;
import com.javaex.model.ReviewDao;
import com.javaex.model.ShopDao;
import com.javaex.model.ShopUserDao;
import com.javaex.model.ShopUserVo;
import com.javaex.model.ShopVo;

@Controller
public class ListController {
	String number;
	

	
	@Autowired
	ShopDao dao;

	@Autowired
	ShopUserDao userDao;

	@Autowired
	NoticeDao noticedao;

	@Autowired
	ReviewDao reviewdao;

	@Autowired
	ReservationDao resDao;

	@RequestMapping("/list")
	public ModelAndView list(ModelAndView mav, HttpServletRequest request) {
		System.out.println("/BabPool/list");
		mav.addObject("shopList",
				dao.shopSearch(request.getParameter("location"), request.getParameterValues("shop_addr"),
						request.getParameterValues("food_type"), request.getParameter("string_search"),
						request.getParameter("solt"), request.getParameter("price_list"),
						request.getParameterValues("add_info"), request.getParameterValues("table_type"),
						request.getParameterValues("alcohol_type"), request.getParameter("parking_available")));
		mav.setViewName("list");
		return mav;
	}

	@RequestMapping("/review_upload")
	public void test(HttpServletRequest req, HttpServletResponse response) throws IOException {
		System.out.println("/BabPool/review_upload");
		String tmp = req.getParameter("star_span");
		String tmp2 = req.getParameter("review_area");
		response.getWriter().write("success");

		System.out.println("별점의 tmp : " + tmp + "\n" + "textarea : " + tmp2);
	}

	@RequestMapping("/detail")
	public ModelAndView detail(ModelAndView mav, HttpServletRequest request, HttpSession session) {
		System.out.println("/BabPool/detail");
		String user_email = (String) session.getAttribute("sessionID");
		String shop_idx = request.getParameter("shopidx");
		if (user_email != null) {
			userDao.update_recentShop_shopIdx(user_email, shop_idx);
			ShopUserVo user;
			user = userDao.loginCheck(user_email);
			if (user.getRecent_shop() != null) {
				ShopVo recent_shopList = dao.getAll_shopIdx(user.getRecent_shop());
				session.setAttribute("shop_title", recent_shopList.getShop_title());
				session.setAttribute("food_type", recent_shopList.getFood_type());
				session.setAttribute("shop_addr", recent_shopList.getShop_addr());
				session.setAttribute("shop_idx", recent_shopList.getShop_idx());
			}
		}
		int shopId = Integer.parseInt(shop_idx);
		mav.addObject("shopOne", dao.shopOne(shopId));
		mav.setViewName("detail/detail");
		return mav;
	}

	@RequestMapping("/login")
	public void login(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException {
		System.out.println("/BabPool/login");

		String user_email = request.getParameter("user_id");
		String password = request.getParameter("user_pw");

		ShopUserVo user;
		if (userDao.loginCheck(user_email) != null) {
			user = userDao.loginCheck(user_email);

			if (user.getUser_pw().equals(password)) {
				response.getWriter().write("success");
				session.setAttribute("is_owner", user.getIs_owner());
				session.setAttribute("sessionID", user_email);
				if (user.getRecent_shop() != null) {
					ShopVo recent_shopList = dao.getAll_shopIdx(user.getRecent_shop());
					session.setAttribute("shop_title", recent_shopList.getShop_title());
					session.setAttribute("food_type", recent_shopList.getFood_type());
					session.setAttribute("shop_addr", recent_shopList.getShop_addr());
					session.setAttribute("shop_idx", recent_shopList.getShop_idx());
				}

				if (user.getIs_owner().equals("1")) {
					session.setAttribute("shop_id", dao.getShopId(user_email));
				}
			} else {
				response.getWriter().write("fail");
			}
		} else if (user_email.equals("admin")) {
			response.getWriter().write("admin");
		} else {
			System.out.println("ID가 존재하지않음");
			response.getWriter().write("fail");
		}
	}

	@RequestMapping("/logout")
	public ModelAndView logout(ModelAndView mav, HttpSession session) {
		System.out.println("/BabPool/logout");
		session.invalidate();
		mav.setViewName("main");
		return mav;
	}

	@RequestMapping("/join")
	public ModelAndView signup(ModelAndView mav, HttpServletRequest req) {
		System.out.println("/BabPool/signup");
		String email = req.getParameter("email");
		String pw = req.getParameter("pw");
		String name = req.getParameter("name");
		String birth = req.getParameter("bir_year") + "-" + req.getParameter("bir_mon") + "-"
				+ req.getParameter("bir_day");
		String gender = req.getParameter("gender");
		String phone = req.getParameter("phone");
		int joinType = Integer.parseInt(req.getParameter("join_type"));

		if (joinType == 1) {
			userDao.signUp(new ShopUserVo(email, pw, name, gender, birth, phone, "0", null, 0, null, 0));
		} else {
			String buisnessNumber = req.getParameter("buisness_number");
			String buisnessName = req.getParameter("buisness_name");
			String buisnessAddress = req.getParameter("buisness_address");
			String buisnessAddressEtc = req.getParameter("buisness_address_etc");
			String buisnessFoodType = req.getParameter("buisness_food_type");

			System.out.println(buisnessNumber + " " + buisnessName + " " + buisnessAddress + " " + buisnessAddressEtc
					+ " " + buisnessFoodType);
			userDao.signUp(new ShopUserVo(email, pw, name, gender, birth, phone, "1", null, 0, null, 0));
		}
		mav.setViewName("main");
		return mav;
	}

	// 공지사항
	@RequestMapping("/notice")
	public ModelAndView notice(ModelAndView mav) {
		System.out.println("/BabPool/notice => Notice_Page");
		mav.addObject("Notice", noticedao.noticeList());
		mav.setViewName("notice");
		return mav;
	}

	@RequestMapping("/detail/info.do")
	public ModelAndView detail_info(ModelAndView mav, HttpServletRequest request) {
		System.out.println("/BabPool/detail_info");
		int shop_idx = Integer.parseInt(request.getParameter("shopidx"));
		mav.addObject("shopOne", dao.shopOne(shop_idx));
		mav.setViewName("detail/detail_info");
		return mav;
	}

	// @RequestMapping("/detail2/photo.do")
	// public ModelAndView detail_photo(ModelAndView mav,HttpServletRequest
	// request) {
	// System.out.println("/BabPool/detail_photo");
	// int shop_idx = Integer.parseInt(request.getParameter("shopidx"));
	// mav.addObject("shopOne",shopdao.shopOne(shop_idx));
	// mav.setViewName("detail_photo");
	// return mav;
	// }

	@RequestMapping("/buisness_update")
	public ModelAndView buisnessmypage_update(ModelAndView mav, HttpServletRequest req) {
		String shop_title = req.getParameter("shop_title");
		String shop_addr = req.getParameter("shop_addr");
		String shop_location = req.getParameter("shop_location");
		String shop_id = req.getParameter("shop_id");
		String food_type = req.getParameter("food_type");
		String budget = req.getParameter("budget");
		String shop_tip = req.getParameter("shop_tip");
		String shop_comment = req.getParameter("shop_comment");
		String shop_phone = req.getParameter("shop_phone");
		String[] shop_time = req.getParameterValues("shop_time");
		String[] shop_addinfoArr = req.getParameterValues("shop_addinfo");
		String shop_addinfo = "";
		String[] shop_tbArr = req.getParameterValues("shop_tb");
		String shop_tb = "";
		String[] shop_alcoholArr = req.getParameterValues("shop_alcohol");
		String shop_alcohol = "";
		String shop_car = req.getParameter("shop_car");
		String shop_close = req.getParameter("shop_close");
		String shop_photo = null;
		String comma = "";

		for (int i = 0; i < shop_alcoholArr.length; i++) {
			if (i == 0) {
				shop_alcohol += comma + shop_alcoholArr[i];
				comma = ",";
			} else {
				shop_alcohol += comma + shop_alcoholArr[i];
			}
		}
		for (int i = 0; i < shop_tbArr.length; i++) {
			if (i == 0) {
				comma = "";
				shop_tb += comma + shop_tbArr[i];
				comma = ",";
			} else {
				shop_tb += comma + shop_tbArr[i];
			}
		}
		for (int i = 0; i < shop_addinfoArr.length; i++) {
			if (i == 0) {
				comma = "";
				shop_addinfo += comma + shop_addinfoArr[i];
				comma = ",";
			} else {
				shop_addinfo += comma + shop_addinfoArr[i];
			}
		}

		ShopVo s = new ShopVo(shop_title, shop_id, shop_addr, shop_location, food_type, shop_tip, budget, shop_comment,
				shop_phone, shop_time, shop_addinfo, shop_tb, shop_alcohol, shop_car, shop_close, shop_photo);
		dao.updateShop(s);
		mav.setViewName("buisnessmypage");
		System.out.println(s.toString());
		return mav;
	}

	@RequestMapping("/buisnessmypage/registration2")
	public ModelAndView registration2(ModelAndView mav, HttpSession session) {

		mav.addObject("shopOwnerList", dao.shopOwnerList((String) session.getAttribute("sessionID")));
		mav.setViewName("buisnessmypage/buisness_mypage_registration2");
		return mav;
	}

	@RequestMapping("/reservation")
	public ModelAndView Reservation(ModelAndView mav, HttpServletRequest req, HttpServletResponse res,
			HttpSession session) throws ParseException {

		String user_email = (String) session.getAttribute("sessionID");
		String shop_title = req.getParameter("shop_title");
		String res_date = req.getParameter("res_date");
		int res_customer = Integer.parseInt(req.getParameter("res_customer"));
		String shop_id = req.getParameter("shop_id");
		String rev_phone = req.getParameter("rev_phone");
		java.util.Date date = new java.util.Date();
		java.sql.Date res_date2 = new java.sql.Date(date.getTime());
		System.out.println(res_date);
		System.out.println(user_email);
		System.out.println(shop_title);
		System.out.println(res_customer);
		System.out.println(shop_id);
		System.out.println(res_date2);
		System.out.println(rev_phone);
		ReservationVo resvo = new ReservationVo(user_email, shop_title, res_date2, res_customer, shop_id, null, null,
				rev_phone);
		resDao.insert_reservation(resvo);

		return mav;
	}

	@RequestMapping("/alert")
	public void Alert(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String shop_id = request.getParameter("shop_id");
		String alert_new = "no";
		List<ReservationVo> alert_list = resDao.getAll_shopId(shop_id);
		if (alert_list != null) {
			for (int i = 0; i < alert_list.size(); i++) {
				if (alert_list.get(i).getAlert_new().equals("1")) {
					alert_new = "exist";
				}
			}
			if (alert_new.equals("exist")) {
				resDao.updateAlertNew_shopId(shop_id);
			}
			response.getWriter().write(alert_new);
		}
	}

	@RequestMapping("/buisnessmypage/reservation2")
	public ModelAndView reservation2(ModelAndView mav, HttpServletRequest req,HttpSession session) {
		System.out.println("/reservation2");
		String a = (String)session.getAttribute("shop_id");
		mav.addObject("reservation", resDao.reservationOne(a));
		List <ReservationVo> aa = resDao.reservationOne(a);
		for(int i=0; i<aa.size(); i++) {
			System.out.println(aa.get(i).getUser_email());
		}
		mav.setViewName("buisnessmypage/buisness_mypage_reservation2");
		return mav;
	}
	
	@RequestMapping("/idSearch")
	public void findemail(HttpServletRequest req,  HttpServletResponse response) throws IOException {
		System.out.println("/idSearch");
		HashMap<String, Object> map = new HashMap<String, Object>();
		String find_email =  req.getParameter("find_email");
		String find_phone =  req.getParameter("find_phone");
		map.put("user_name", find_email);
		map.put("user_phone", find_phone);
		
	
		String user_email = userDao.selectemail(map);
		if(user_email == null) {
			response.getWriter().write("fail");
		}else {
			response.getWriter().write(user_email);
		}
	}
	
	
	@RequestMapping("/pwSearch")
	public void pwSearch(HttpServletRequest req,  HttpServletResponse response) throws IOException {
		System.out.println("/pwSearch");
		String pwsearch_email =  req.getParameter("pwsearch_email");
		String state = "fail";
		
		List<String> user_email = userDao.searchemail();
		if(user_email == null) {
			response.getWriter().write(state);
		}else {
			for(int i =0 ;i < user_email.size();i++) {
				if(user_email.get(i).equals(pwsearch_email)) {
					state = "success";
					number = "1234";
					response.getWriter().write(number);
					return;
				}
			}
			if(state.equals("fail")) {
				response.getWriter().write(state);
			}
			
		}
	}
	
	@RequestMapping("/pwSearch2")
	public void pwSearch2(HttpServletRequest req,  HttpServletResponse response) throws IOException {
		System.out.println("/pwSearch2");
		String email_number =  req.getParameter("email_number");
		String state = "fail";
		
		if(email_number.equals(number)) {
			state = "success";
			response.getWriter().write(state);
		}
		else if(state.equals("fail")) {
			response.getWriter().write(state);
		}
		
	}
	
	@RequestMapping("/pwupdate")
	public void pwupdate(HttpServletRequest req,  HttpServletResponse response) throws IOException {
		System.out.println("/pwupdate");
		String pwsearch_email = req.getParameter("pwsearch_email");
		String repassword = req.getParameter("repassword");
		String repassword2 = req.getParameter("repassword2");
		
		if (repassword.equals(repassword2)) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("user_email", pwsearch_email);
			map.put("user_pw", repassword);
			userDao.updatepassword(map);
			response.getWriter().write("success");
		} else {
			response.getWriter().write("fail");
		}
	
	
	}
	
}

