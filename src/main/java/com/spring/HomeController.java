package com.spring;

import java.util.List;
import java.util.Map;
import java.util.Random;

import java.time.LocalDate;  
import java.time.format.DateTimeFormatter;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.spring.services.MailerService;

@Controller
public class HomeController {
	@Autowired
    private MailerService senderService;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	String ilce, sirket, durum, depo;
	float ucret, kargoFiyat, kargoUcretsiz;

	@RequestMapping(value = "/")
    public String getHomePage(Model model) {		
		ucret = 1 + new Random().nextFloat() * (1500 - 1);
		ucret = (float) (Math.floor(ucret * 100) / 100);
		kargoUcretsiz = 1000 - ucret;
		kargoUcretsiz = (float) (Math.floor(kargoUcretsiz * 100) / 100);
		kargoFiyat = (float)12.59;
		if(kargoUcretsiz <= 0) {kargoFiyat = 0; kargoUcretsiz=0;}	
		
		model.addAttribute("ucret", ucret);
		model.addAttribute("kargoFiyat", kargoFiyat);
		model.addAttribute("kargoUcretsiz", kargoUcretsiz);
        return "ekran1";
    }
	
	@GetMapping("/takeIlce")
    public String takeIlce(@RequestParam(name="ilce", required = false) String ilce, Model model) {
		this.ilce = ilce;
		model.addAttribute("ucret", ucret);
		model.addAttribute("kargoFiyat", kargoFiyat);
		model.addAttribute("kargoUcretsiz", kargoUcretsiz);
		
        return "ekran1";
    }
	@GetMapping("/onayla")
    public String onayla(@RequestParam(name="kargo", required = false) String kargo, @RequestParam(name="mail", required = false) String mail, Model model) {
		this.sirket = kargo;
		
		Integer takipno = 1000000 + (int)(new Random().nextFloat() * (9999999 - 1000000));		
		int sirketId, ilceId;
		
		if(sirket.equals("Jetizz")) {sirketId=1; kargoFiyat=(float)9.45;}
		else if(sirket.equals("Aras Kargo")) {sirketId=2; kargoFiyat=(float)15.00;}
		else {sirketId=3; kargoFiyat=(float)12.59;}

		if(ilce.equals("Ataşehir")) {ilceId=1;}
		else if(ilce.equals("Bahçelievler")) {ilceId=2;}
		else if(ilce.equals("Bakırköy")) {ilceId=3;}
		else if(ilce.equals("Beşiktaş")) {ilceId=4;}
		else if(ilce.equals("Beyoğlu")) {ilceId=5;}
		else if(ilce.equals("Çekmeköy")) {ilceId=6;}
		else if(ilce.equals("Fatih")) {ilceId=7;}
		else if(ilce.equals("Kadıköy")) {ilceId=8;}
		else if(ilce.equals("Ümraniye")) {ilceId=9;}
		else {ilceId=10;}	

		for (char ch : mail.toCharArray()) {
			if(ch == '@') {
				String mailsql = "INSERT IGNORE INTO `kargo`.`mail_table` (`mail`) VALUES (?)";
				jdbcTemplate.update(mailsql, mail);
				String mailIdSql = "SELECT `id` FROM `kargo`.`mail_table` WHERE `mail`='" + mail + "';";
				String mailId = jdbcTemplate.queryForObject(mailIdSql, String.class);
					
				String sql = "INSERT INTO `kargo`.`takipno_table` (`takipno`, `mailId`, `sirketId`, `durumId`, `ilceId`, `kargoucreti`) VALUES (?, ?, ?, ?, ?, ?)";
				jdbcTemplate.update(sql, takipno, Integer.parseInt(mailId), sirketId, 1, ilceId, String.valueOf(kargoFiyat));
				
				senderService.sendEmail(mail, "Kargo Takip Numarası", takipno.toString());
				
				return "ekran3";
			}
		}
		
		model.addAttribute("ucret", ucret);
		model.addAttribute("kargoFiyat", kargoFiyat);
		model.addAttribute("kargoUcretsiz", kargoUcretsiz);
		
        return "ekran1";
    }
	
	@GetMapping("/ekran2")
    public String ekran2() {
        return "ekran2";
    }
	
	@GetMapping("/sorgula")
    public String ekran4(@RequestParam(name="kargono", required = true) String kargono, Model model) {
		String sirketIdSql = "SELECT `sirketId` FROM `kargo`.`takipno_table` WHERE `takipno`='" + kargono + "';";
		String sirketId = jdbcTemplate.queryForObject(sirketIdSql, String.class);
		String sirketsql = "SELECT `sirket` FROM `kargo`.`sirket_table` WHERE `id`='" + sirketId + "';";
		String sirket = jdbcTemplate.queryForObject(sirketsql, String.class);
		
		String durumIdSql = "SELECT `durumId` FROM `kargo`.`takipno_table` WHERE `takipno`='" + kargono + "';";
		String durumId = jdbcTemplate.queryForObject(durumIdSql, String.class);
		String durumSql = "SELECT `durum` FROM `kargo`.`durum_table` WHERE `id`='" + durumId + "';";
		String durum = jdbcTemplate.queryForObject(durumSql, String.class);
		this.durum = durum;
		
		String ilceIdSql = "SELECT `ilceId` FROM `kargo`.`takipno_table` WHERE `takipno`='" + kargono + "';";
		String ilceId = jdbcTemplate.queryForObject(ilceIdSql, String.class);
		String ilceSql = "SELECT `ilce` FROM `kargo`.`ilce_table` WHERE `id`='" + ilceId + "';";
		String ilce = jdbcTemplate.queryForObject(ilceSql, String.class);
		this.ilce = ilce;
		
		String kargoucretiSql = "SELECT `kargoucreti` FROM `kargo`.`takipno_table` WHERE `takipno`='" + kargono + "';";
		String kargoucreti = jdbcTemplate.queryForObject(kargoucretiSql, String.class);
		
		String message, message2, depo;
		int sure;
		
		int kargonoInt = Integer.parseInt(kargono);		
		if(kargonoInt % 2 == 1) {depo = "Beykoz";}
		else {depo = "Güngören";}
		this.depo = depo;
		
		if(durum.equals("Kargoya Verilmedi")) {
			message = "SİPARİŞİNİZ HENÜZ KARGOYA VERİLMEMİŞTİR.";
			message2 = "Kargonuz henüz " + depo + "'dan çıkmamıştır.";
		}
		else if(durum.equals("Kargoya Verildi")) {
			message = "SİPARİŞİNİZ KARGOYA VERİLMİŞTİR.";
			message2 = "Kargonuz " + depo + "'dan " + ilce + "'e ulaştırılmak üzere yola çıkmıştır.";
		}
		else {
			message = "SİPARİŞİNİZ TESLİM EDİLMİŞTİR.";
			message2 = "Kargonuz " + depo + "'dan " + ilce + "'e ulaştırılmıştır.";
		}
		
		int mesafe = 1 + (int)(new Random().nextFloat() * (200 - 1));
		if(mesafe <= 30) {sure=1;}
		else if(mesafe <= 50) {sure=2;}
		else {sure=3;}
		
		model.addAttribute("message", message);
		model.addAttribute("sirket", sirket);
		model.addAttribute("sure", String.valueOf(sure));
		model.addAttribute("message2", message2);
		model.addAttribute("durum", durum);
		model.addAttribute("kargoucreti", kargoucreti);
				
        return "ekran4";
    }
	
	@GetMapping("/ekran5")
    public String ekran5(Model model) {
		String message, message2="", message3 = "";
		
		if(durum.equals("Kargoya Verilmedi")) {
			message = "🡢Kargonuz henüz " + depo + "'dan çıkmamıştır.";
		}		
		else if(ilce.equals("Çekmeköy")) {
			message = "🡢Kargonuz " + depo + "'daki depodan çıkıp Ümraniye'ye ulaşmıştır.";
		}
		else if(ilce.equals("Kadıköy")) {
			message = "🡢Kargonuz " + depo + "'daki depodan çıkıp Ataşehir'e ulaşmıştır.";
			message2 = "🡢Kargonuz Ataşehir'den çıkıp Üsküdar'a ulaşmıştır.";
		}
		else if(ilce.equals("Beşiktaş")) {
			message = "🡢Kargonuz " + depo + "'daki depodan çıkıp Beyoğlu'na ulaşmıştır.";
		}
		else {
			message = "🡢Kargonuz " + depo + "'daki depodan yola çıkmıştır.";
		}
		
		if(durum.equals("Teslim Edildi")) {
			message3 ="🡢Kargonuz " + ilce + "'e teslim edilmiştir.";
		}
		
		model.addAttribute("message", message);
		if(!message2.equals("")) {
			model.addAttribute("message2", message2);
			model.addAttribute("message3", message3);
		}
		else {
			model.addAttribute("message2", message3);
			model.addAttribute("message3", "");
		}
		
        return "ekran5";
    }
	
	String kargono;
	
	@GetMapping("/iadeEt")
    public String ekran6(@RequestParam(name="kargono", required = true) String kargono, Model model) {
		this.kargono = kargono;
		String sepetIdSql = "SELECT `sepetId` FROM `kargo`.`takipno_table` WHERE `takipno`='" + kargono + "';";
		String sepetId = jdbcTemplate.queryForObject(sepetIdSql, String.class);
		
		String sepetsql = "SELECT `isim` FROM `kargo`.`urun_table` WHERE `sepetId`='" + sepetId + "';";
		List<Map<String, Object>> names = jdbcTemplate.queryForList(sepetsql);
		String nameDell = names.get(0).get("isim").toString();
		String nameLenovo = names.get(1).get("isim").toString();
		String nameLogitech = names.get(2).get("isim").toString();
		
		String sepetsql2 = "SELECT `ucret` FROM `kargo`.`urun_table` WHERE `sepetId`='" + sepetId + "';";
		List<Map<String, Object>> prices = jdbcTemplate.queryForList(sepetsql2);
		String priceDell = prices.get(0).get("ucret").toString();
		String priceLenovo = prices.get(1).get("ucret").toString();
		String priceLogitech = prices.get(2).get("ucret").toString();

		model.addAttribute("nameDell", nameDell);
		model.addAttribute("nameLenovo", nameLenovo);
		model.addAttribute("nameLogitech", nameLogitech);
		model.addAttribute("priceDell", priceDell);
		model.addAttribute("priceLenovo", priceLenovo);
		model.addAttribute("priceLogitech", priceLogitech);
		
        return "ekran6";
    }
	
	@GetMapping("/talepOlustur")
    public String ekran7(@RequestParam(name="urun", required = true) List<String> uruns, Model model) {
		String sepetIdSql = "SELECT `sepetId` FROM `kargo`.`takipno_table` WHERE `takipno`='" + kargono + "';";
		String sepetId = jdbcTemplate.queryForObject(sepetIdSql, String.class);
		
		String sepetsql = "SELECT `isim` FROM `kargo`.`urun_table` WHERE `sepetId`='" + sepetId + "';";
		List<Map<String, Object>> names = jdbcTemplate.queryForList(sepetsql);
		String nameDell = names.get(0).get("isim").toString();
		String nameLenovo = names.get(1).get("isim").toString();
		String nameLogitech = names.get(2).get("isim").toString();
		
		String sepetsql2 = "SELECT `ucret` FROM `kargo`.`urun_table` WHERE `sepetId`='" + sepetId + "';";
		List<Map<String, Object>> prices = jdbcTemplate.queryForList(sepetsql2);
		String priceDell = prices.get(0).get("ucret").toString();
		String priceLenovo = prices.get(1).get("ucret").toString();
		String priceLogitech = prices.get(2).get("ucret").toString();
		
		String tarihSureSql = "SELECT `tarih` FROM `kargo`.`takipno_table` WHERE `takipno`='" + kargono + "';";
		String tarih = jdbcTemplate.queryForObject(tarihSureSql, String.class);
		
		LocalDate d1 = LocalDate.parse(tarih, DateTimeFormatter.ISO_LOCAL_DATE);
		LocalDate d2 = LocalDate.now();
		Duration diff = Duration.between(d1.atStartOfDay(), d2.atStartOfDay());
		long diffDays = diff.toDays();
		
		if(diffDays >= 14) {
			
			model.addAttribute("nameDell", nameDell);
			model.addAttribute("nameLenovo", nameLenovo);
			model.addAttribute("nameLogitech", nameLogitech);
			model.addAttribute("priceDell", priceDell);
			model.addAttribute("priceLenovo", priceLenovo);
			model.addAttribute("priceLogitech", priceLogitech);
			
			String dell="0", lenovo="0", logitech="0";	
			for(String u: uruns) {
				if(u.equals("dell"))
					dell = "1";
				if(u.equals("lenovo"))
					lenovo = "1";
				if(u.equals("logitech"))
					logitech = "1";
			}
			
			model.addAttribute("dell", dell);
			model.addAttribute("lenovo", lenovo);
			model.addAttribute("logitech", logitech);
			
	        return "ekran7";
		}
		else {
			long leftLimit = 100000000000L;
		    long rightLimit = 999999999999L;
			long gonderiKodu = leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
			
			model.addAttribute("gonderiKodu", gonderiKodu);
			
			return "ekran8";
		}
	      
		
		
		
    }
	
}
