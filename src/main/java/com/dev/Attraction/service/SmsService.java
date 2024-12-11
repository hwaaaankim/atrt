package com.dev.Attraction.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SmsService {
	
	private final static String apiUrl="https://sslsms.cafe24.com/sms_sender.php";
	private final static String userAgent="Mozilla/5.0";
	private final static String charset="UTF-8";
	private final static boolean isTest=true;
	
	Base64 base64 = new Base64();
	
	public void sendMessage(String phone, String msg) throws EncoderException {
		try {
			URL obj = new URL(apiUrl);
			HttpURLConnection con = (HttpURLConnection)obj.openConnection();
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setRequestProperty("Accept-Charset", charset);
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", userAgent);
			String postParams = 
					"user_id="+new String(Base64.encodeBase64("teriwoo9".getBytes()))
					+"&secure="+new String(Base64.encodeBase64("dd8ee7a677db6ae5abbbfd5122150caf".getBytes()))
					+"&msg="+new String(Base64.encodeBase64(msg.getBytes()))
					+"&rphone="+new String(Base64.encodeBase64(phone.getBytes()))
					+"&sphone1="+new String(Base64.encodeBase64("010".getBytes()))
					+"&sphone2="+new String(Base64.encodeBase64("3894".getBytes()))
					+"&sphone3="+new String(Base64.encodeBase64("3849".getBytes()))
					+"&mode="+new String(Base64.encodeBase64("1".getBytes()))
					+"&smsType=S";
			if(isTest) {
				postParams +="$testflag="+new String(Base64.encodeBase64("Y".getBytes()));
			}
			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			os.write(postParams.getBytes());
			os.flush();
			os.close();
			
			int responseCode=con.getResponseCode();
			log.info("POST Response Code :: " + responseCode);
			
			if(responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer buf = new StringBuffer();
				
				while((inputLine = in.readLine()) != null) {
					buf.append(inputLine);
				}
				
				in.close();
				log.info("SMS Content : "+buf.toString());
			}else {
				log.error("POST request not worked");
			}
			
		}catch(IOException ex) {
			log.error("SMS IOException : " + ex.getMessage());
		}
		
	}
}
