package uiBank;

import java.io.File;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class UIBankOperations 
{
	File tokenBody = new File("jsonDataFiles/tokenBody.json");
	File loanBody = new File("jsonDataFiles/applyForLoanBody.json");
	String accessToken,userId,quoteid;
	@Test
	public void generateAccessToken()
	{
		RestAssured.baseURI = "https://uibank-api.azurewebsites.net";
		Response responseTokenGeneration = RestAssured
										   .given()
										   .header("Content-Type","application/json")
										   .header("Accept","application/json")
										   .body(tokenBody)
										   .post("api/users/login");
		
		
		JsonPath responseTokenGenerationJson = responseTokenGeneration.jsonPath();
			
		if(responseTokenGeneration.getStatusCode()==200)
		{
			System.out.println("1) The login and Access Token Generation is successfull!!");
			accessToken = responseTokenGenerationJson.getString("id");
			userId = responseTokenGenerationJson.getString("userId");
			RestAssured.authentication = RestAssured.oauth2(accessToken);
		}
		
		else
			System.out.println("1) The login and Access Token Generation Failed!!");
	}
	
	@Test(priority=1,dependsOnMethods={"generateAccessToken"})
	public void getAccountDetails()
	{
		Response responseGetAccountDetails = RestAssured
				   						   .given()
				   						   .header("Content-Type","application/json")
				   						   .header("Accept","application/json")
				   						   .queryParam("filter[where][userId]", userId)
				   						   .get("/api/accounts/");
		
		JsonPath responseGetAccountDetailsJson = responseGetAccountDetails.jsonPath();
		if(responseGetAccountDetails.getStatusCode()==200)
		{
			System.out.println("\n\n2) The details of the account are mentioned as below:");
			System.out.println("--->Account Holder Name: "+responseGetAccountDetailsJson.getString("[0].friendlyName"));
			System.out.println("--->Account Number: "+responseGetAccountDetailsJson.getString("[0].accountNumber"));
			System.out.println("--->Account Type: "+responseGetAccountDetailsJson.getString("[0].type"));
			System.out.println("--->Balance: "+responseGetAccountDetailsJson.getString("[0].balance")+" $");	
		}
		
		else
			System.out.println("\n\n2) The details of the account couldn't be retrived!!:");
	}
	
	@Test(priority=2,dependsOnMethods={"generateAccessToken"})
	public void applyForLoan()
	{
		Response responseApplyForLoan = RestAssured
				   						   .given()
				   						   .header("Content-Type","application/json")
				   						   .header("Accept","application/json")
				   						   .body(loanBody)
										   .post("api/quotes/newquote");
		
		if(responseApplyForLoan.getStatusCode()==200)
		{
			System.out.println("\n\n3) The Application for Loan Succeeded!!");
			JsonPath responseApplyForLoanJson = responseApplyForLoan.jsonPath();
			quoteid = responseApplyForLoanJson.getString("quoteid");
		}
		else
			System.out.println("\n\n3) The Application for Loan Failed!!");
		
	}
	
	@Test(priority=3,dependsOnMethods={"generateAccessToken","applyForLoan"})
	public void getLoanStatus()
	{
		Response responseGetAccountDetails = RestAssured
				   						   .given()
				   						   .header("Content-Type","application/json")
				   						   .header("Accept","application/json")
				   						   .get("/api/quotes/"+quoteid);
		
		if(responseGetAccountDetails.getStatusCode()==200)
		{
			System.out.println("\n\n4) The details of the applied Loan is mentioned below:");
			JsonPath responseGetAccountDetailsJson = responseGetAccountDetails.jsonPath();
			String loanAmount = responseGetAccountDetailsJson.getString("amount");
			String loanTerm = responseGetAccountDetailsJson.getString("term");
			String rateOfInterest = responseGetAccountDetailsJson.getString("rate");
		
			System.out.println("--->Loan amount: "+loanAmount+" $");
			System.out.println("--->Loan Term: "+loanTerm+" year(s)");
			System.out.println("--->Rate of Interest: "+rateOfInterest+" %");
			System.out.println("\n\n");
		}
		
		else
			System.out.println("\n\n4) The details of the Loan couldn't be retrived!!\n\n");
	}
	
}
