import static spark.Spark.*;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import spark.Spark;

public class Server {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Server Started............ ");

		staticFiles.externalLocation(Constants.external_file_location);

		options("/*",
				(request, response) -> {

					String accessControlRequestHeaders = request
							.headers("Access-Control-Request-Headers");
					if (accessControlRequestHeaders != null) {
						response.header("Access-Control-Allow-Headers",
								accessControlRequestHeaders);
					}

					String accessControlRequestMethod = request
							.headers("Access-Control-Request-Method");
					if (accessControlRequestMethod != null) {
						response.header("Access-Control-Allow-Methods",
								accessControlRequestMethod);
					}

					return "OK";
				});

		before((request, response) -> response.header(
				"Access-Control-Allow-Origin", "*"));

		// code starts from here....

		get("/hello", (req, res) -> "Hello World");

		post("/login",
				(request, response) -> {
					System.out.println(request.body() + "---");
					String body = request.body();

					JSONObject responseData = new JSONObject();

					System.out.println("received data as " + body);
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);
						System.out.println("Data is parsed sucess ");

						if (jsonData.get("username") == null
								|| jsonData.get("password") == null) {
							responseData.put("result", false);
							responseData.put("description",
									"Please send username and password");
						} else {
							String userName = (String) jsonData.get("username");
							String password = (String) jsonData.get("password");
							JSONObject payload = new JSONObject();

							Dbcon db = new Dbcon();
							String sql = "select * from tbl_student where email='"
									+ userName
									+ "' and password='"
									+ password
									+ "'";
							ResultSet rs = db.select(sql);
							if (rs.next()) {
								responseData.put("result", true);
								responseData.put("description",
										"Login was sucess");
								payload.put("name", rs.getString("name"));
								payload.put("userId", rs.getString("id"));
								payload.put("branch", rs.getString("branch"));
								payload.put("address", rs.getString("address"));
								payload.put("phone", rs.getString("phone_no"));
								payload.put("photo", rs.getString("photo"));
								payload.put("union_member",
										rs.getString("union_member"));
								payload.put("admissionDateMilli",
										rs.getString("admdate_milli"));
								payload.put("college", rs.getString("college"));
								payload.put("college_id",
										rs.getString("college_id"));
								payload.put("semester",
										rs.getString("semester"));
								responseData.put("payload", payload);
							} else {
								responseData.put("result", false);
								responseData.put("description",
										"Login failed, Incorrect credentials");
							}
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					}

					return responseData;
				});

		post("/changePassword ",
				(request, response) -> {
					System.out.println(request.body() + "---");
					String body = request.body();

					JSONObject responseData = new JSONObject();

					System.out.println("received data as " + body);
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);
						System.out.println("Data is parsed sucess ");

						if (jsonData.get("userId") == null
								|| jsonData.get("currentpassword") == null
								|| jsonData.get("newpassword") == null) {
							responseData.put("result", false);
							responseData.put("description",
									"Please send all the required values");
						} else {
							String userId = (String) jsonData.get("userId");
							String currentpassword = (String) jsonData
									.get("currentpassword");
							String newpassword = (String) jsonData
									.get("newpassword");
							JSONObject payload = new JSONObject();

							Dbcon db = new Dbcon();
							String sql = "select * from tbl_student where id='"
									+ userId + "' and password='"
									+ currentpassword + "'";
							ResultSet rs = db.select(sql);
							if (rs.next()) {

								int updt = db
										.update("update tbl_student set password='"
												+ newpassword
												+ "' where id="
												+ userId);
								if (updt > 0) {
									responseData.put("result", true);
									responseData.put("description",
											"Password changed successfully");
								} else {
									responseData.put("result", false);
									responseData.put("description",
											"Could not change passord, please try later");
								}

							} else {
								responseData.put("result", false);
								responseData
										.put("description",
												"Could not change password, please check current password");
							}
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					}

					return responseData;
				});

		post("/upload",
				"multipart/form-data",
				(request, response) -> {
					// - Servlet 3.x config
				try {
					String location = Constants.external_file_location;
					long maxFileSize = 100000000;
					long maxRequestSize = 100000000;
					int fileSizeThreshold = 1024;

					MultipartConfigElement multipartConfigElement = new MultipartConfigElement(
							location, maxFileSize, maxRequestSize,
							fileSizeThreshold);
					request.raw().setAttribute(
							"org.eclipse.jetty.multipartConfig",
							multipartConfigElement);

					Collection<Part> parts = request.raw().getParts();
					for (Part part : parts) {

						System.out.println("-------------------------");
						System.out.println(part);
						System.out.println("Name:");
						System.out.println(part.getName());
						System.out.println("Size: ");
						System.out.println(part.getSize());
						System.out.println("Filename:");
						System.out.println(part.getSubmittedFileName());
					}

					System.out.println("request.body() ************** "
							+ request.body());

					String fName = request.raw().getPart("upfile")
							.getSubmittedFileName();
					System.out.println("Title: "
							+ request.raw().getParameter("title"));
					System.out.println("File: " + fName);

					Part uploadedFile = request.raw().getPart("upfile");
					Path out = Paths.get(Constants.external_file_location
							+ request.raw().getParameter("title"));
					try (final InputStream in = uploadedFile.getInputStream()) {
						Files.copy(in, out);
						uploadedFile.delete();
					}
					// cleanup
					multipartConfigElement = null;
					parts = null;
					uploadedFile = null;
				} catch (Exception e) {
					e.printStackTrace();
				}

				return "OK";
			});

		post("/getNotifications",
				(request, response) -> {
					System.out.println("getNotifications  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						if (jsonData.get("userId") == null) {
							responseData.put("result", false);
							responseData.put("description",
									"Please send user ID");
						} else {
							JSONObject payload = new JSONObject();
							JSONArray dataarray = new JSONArray();
							Dbcon db = new Dbcon();

							String sql = "select * from tbl_notifications order by id desc";
							ResultSet rs = db.select(sql);
							while (rs.next()) {
								JSONObject notify = new JSONObject();
								notify.put("title", rs.getString("title"));
								notify.put(
										"description",
										rs.getString("description").replaceAll(
												"(\r\n|\n\r|\r|\n)", "<br />"));
								dataarray.add(notify);
							}
							responseData.put("result", true);
							responseData.put("description",
									"Sucessfully fetched ");
							responseData.put("payload", dataarray);
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					}

					return responseData;
				});

		post("/getMyNews",
				(request, response) -> {
					System.out.println("getNews API call " + request.body()
							+ " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						if (jsonData.get("userId") == null
								|| jsonData.get("college_id") == null) {
							responseData.put("result", false);
							responseData.put("description",
									"Please send college id and user id");
						} else {
							JSONObject payload = new JSONObject();
							JSONArray dataarray = new JSONArray();
							Dbcon db = new Dbcon();

							String sql = "select * from tbl_news where owner_id='"
									+ jsonData.get("userId")
									+ "' order by id desc";

							ResultSet rs = db.select(sql);
							while (rs.next()) {
								JSONObject news = new JSONObject();

								news.put("title", rs.getString("title"));
								news.put("description",
										rs.getString("description"));
								news.put("owner_id", rs.getString("owner_id"));
								news.put("owner_type",
										rs.getString("owner_type"));
								news.put("date_milli",
										rs.getString("date_milli"));
								news.put("pic", rs.getString("pic"));
								news.put("id", rs.getString("id"));

								dataarray.add(news);
							}
							responseData.put("result", true);
							responseData.put("description",
									"Sucessfully fetched news");
							responseData.put("payload", dataarray);
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					}

					return responseData;
				});

		post("/getNews",
				(request, response) -> {
					System.out.println("getNews API call " + request.body()
							+ " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						if (jsonData.get("userId") == null
								|| jsonData.get("college_id") == null) {
							responseData.put("result", false);
							responseData.put("description",
									"Please send college id and user id");
						} else {
							JSONObject payload = new JSONObject();
							JSONArray dataarray = new JSONArray();
							Dbcon db = new Dbcon();

							String sql = "select * from tbl_news where audience='All' or audience='"
									+ jsonData.get("college_id")
									+ "' order by id desc";
							ResultSet rs = db.select(sql);
							while (rs.next()) {
								JSONObject news = new JSONObject();
								news.put("title", rs.getString("title"));
								news.put("description",
										rs.getString("description"));
								news.put("owner_id", rs.getString("owner_id"));
								news.put("owner_type",
										rs.getString("owner_type"));
								news.put("date_milli",
										rs.getString("date_milli"));
								news.put("pic", rs.getString("pic"));

								dataarray.add(news);
							}
							responseData.put("result", true);
							responseData.put("description",
									"Sucessfully fetched ");
							responseData.put("payload", dataarray);
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					}

					return responseData;
				});

		post("/deletePost", (request, response) -> {
			System.out.println("delete post  API call " + request.body()
					+ " --- end ");
			String body = request.body(); // all values passed from angularJS

				JSONObject responseData = new JSONObject(); // to return
															// response as json
															// object

				JSONParser jsonParser = new JSONParser(); // to check input data
															// is valid json or
															// not
				JSONObject payload = new JSONObject(); // relevent data is
														// passed inside payload
				try {
					JSONObject jsonData = (JSONObject) jsonParser.parse(body); // to
																				// check
																				// //
																				// not

					if (jsonData.get("newsId") == null) {

						responseData.put("result", false);
						responseData.put("description",
								"Please send news id to delete news");
					} else {
						Dbcon db = new Dbcon();
						String newsId = jsonData.get("newsId").toString();

						String sql = "delete from tbl_news where id = '"
								+ newsId + "'";

						int upt = db.update(sql);
						if (upt > 0) {
							// update aayi
							responseData.put("result", true);
							responseData.put("description",
									"Successfully deleted news");
						} else {
							// update aayilla
							responseData.put("result", false);
							responseData.put("description",
									"Could not deleted news");
						}
					}
				} catch (ParseException pe) {
					System.out.println("Error in parseing json data");
					System.out.println(pe);
					responseData.put("result", false);
					responseData.put("description", "Please send a valid json");
				}

				return responseData;
			});

		post("/editUserDetails",
				(request, response) -> {
					System.out.println("editUserDetails  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();
					JSONObject payload = new JSONObject();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						if (jsonData.get("userId") == null
								|| jsonData.get("name") == null
								|| jsonData.get("address") == null
								|| jsonData.get("phone") == null) {

							responseData.put("result", false);
							responseData.put("description",
									"Please send all the user details");
						} else {
							Dbcon db = new Dbcon();

							String sql = "update tbl_student set name='"
									+ jsonData.get("name") + "' , address='"
									+ jsonData.get("address")
									+ "' , phone_no='" + jsonData.get("phone")
									+ "' where id = " + jsonData.get("userId");

							int update = db.update(sql);
							if (update <= 0) {
								responseData.put("result", false);
								responseData
										.put("description",
												"Could not update now, Please try again later");
							} else {

								sql = "select * from tbl_student where id="
										+ jsonData.get("userId");
								ResultSet rs = db.select(sql);

								if (rs.next()) {
									responseData.put("result", true);
									responseData
											.put("description",
													"Student details updated successfully");
									payload.put("name", rs.getString("name"));
									payload.put("userId", rs.getString("id"));
									payload.put("branch",
											rs.getString("branch"));
									payload.put("address",
											rs.getString("address"));
									payload.put("phone",
											rs.getString("phone_no"));
									payload.put("admissionDateMilli",
											rs.getString("admdate_milli"));
									payload.put("college",
											rs.getString("college"));
									responseData.put("payload", payload);
								} else {
									responseData.put("result", false);
									responseData
											.put("description",
													"Could not update now, Please try again later");
								}

							}
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					}

					return responseData;
				});

		post("/getSyllabus",
				(request, response) -> {
					System.out.println("getSyllabus  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);
						System.out.println("23233");
						if (jsonData.get("subject") == null
								|| jsonData.get("revision_code") == null) {
							responseData.put("result", false);
							responseData.put("description",
									"Please send subject and revision_code");
						} else {
							JSONObject payload = new JSONObject();
							Dbcon db = new Dbcon();

							String sql = "select * from tbl_sylabus where revision_code='"
									+ jsonData.get("revision_code")
									+ "' and subject_name='"
									+ jsonData.get("subject") + "'";
							System.out.println(sql);
							ResultSet rs = db.select(sql);
							JSONObject syllabus = new JSONObject();
							if (rs.next()) {
								syllabus.put("module_1",
										rs.getString("module_1"));
								syllabus.put("module_2",
										rs.getString("module_2"));
								syllabus.put("module_3",
										rs.getString("module_3"));
								syllabus.put("module_4",
										rs.getString("module_4"));
								syllabus.put("reference",
										rs.getString("reference"));
								syllabus.put("subject_code",
										rs.getString("subject_code"));

								responseData.put("result", true);
								responseData.put("description",
										"Sucessfully fetched ");
								responseData.put("payload", syllabus);
							} else {
								responseData.put("result", false);
								responseData.put("description",
										"No result found");
							}
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					} catch (Exception e) {
						e.printStackTrace();
					}

					return responseData;
				});

		post("/sendFeedback",
				(request, response) -> {
					System.out.println("sendFeedback  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();
					JSONObject payload = new JSONObject();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						if (jsonData.get("userId") == null
								|| jsonData.get("college_id") == null
								|| jsonData.get("feedbackMessage") == null
								|| jsonData.get("feedbackTitle") == null) {

							responseData.put("result", false);
							responseData.put("description",
									"Please send all the details");
						} else {
							Dbcon db = new Dbcon();

							String sql = "insert into tbl_feedback (owner, audience, title, date ,description,college_id) values('"
									+ jsonData.get("userId")
									+ "' , "
									+ " '"
									+ jsonData.get("college_id")
									+ "' , '"
									+ jsonData.get("feedbackTitle")
									+ "' , '"
									+ System.currentTimeMillis()
									+ "' , '"
									+ jsonData.get("feedbackMessage")
									+ "' , '"
									+ jsonData.get("college_id") + "' )";

							int ins = db.insert(sql);
							if (ins <= 0) {
								responseData.put("result", false);
								responseData
										.put("description",
												"Could not send feedback now, Please try again later");
							} else {
								responseData.put("result", true);
								responseData.put("description",
										"Feedback posted successfully");
							}
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					}

					return responseData;
				});

		post("/sendUnionPost",
				(request, response) -> {
					System.out.println("sendUnionPost  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();
					JSONObject payload = new JSONObject();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						if (jsonData.get("title") == null
								|| jsonData.get("postMessage") == null
								|| jsonData.get("audience") == null
								|| jsonData.get("owner_id") == null
								|| jsonData.get("pic") == null) {

							responseData.put("result", false);
							responseData.put("description",
									"Please send all the details");
						} else {
							Dbcon db = new Dbcon();

							String sql = "insert into tbl_news (title, description, owner_id, owner_type, audience,date_milli,pic) values('"
									+ jsonData.get("title")
									+ "' , "
									+ " '"
									+ jsonData.get("postMessage")
									+ "' , '"
									+ jsonData.get("owner_id")
									+ "','student' ,'"
									+ jsonData.get("audience")
									+ "', '"
									+ System.currentTimeMillis()
									+ "' "
									+ " , '" + jsonData.get("pic") + "' )";

							int ins = db.insert(sql);
							if (ins <= 0) {
								responseData.put("result", false);
								responseData
										.put("description",
												"Could not send feedback now, Please try again later");
							} else {
								responseData.put("result", true);
								responseData.put("description",
										"Feedback posted successfully");
							}
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					}

					return responseData;
				});

		post("/getMainTimeTable",
				(request, response) -> {
					System.out.println("getMainTimeTable  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						if (jsonData.get("semester") == null
								|| jsonData.get("branch") == null) {
							responseData.put("result", false);
							responseData.put("description",
									"Please send semester ");
						} else {
							JSONObject payload = new JSONObject();
							JSONArray dataarray = new JSONArray();
							Dbcon db = new Dbcon();

							String sql = "select * from tbl_time_table where semester='"
									+ jsonData.get("semester")
									+ "' and branch='"
									+ jsonData.get("branch")
									+ "'";
							ResultSet rs = db.select(sql);
							while (rs.next()) {
								JSONObject eachExam = new JSONObject();
								eachExam.put("revision_code",
										rs.getString("revision_code"));

								eachExam.put("subject_code",
										rs.getString("subject_code"));

								eachExam.put("subject_name",
										rs.getString("subject_name"));

								eachExam.put("date_milli",
										rs.getString("date_milli"));

								eachExam.put("time", rs.getString("time"));

								eachExam.put("date", rs.getString("date"));

								dataarray.add(eachExam);
							}
							responseData.put("result", true);
							responseData.put("description",
									"Sucessfully fetched ");
							responseData.put("payload", dataarray);
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					}

					return responseData;
				});

		post("/getNextSeriesExamDate",
				(request, response) -> {
					System.out.println("getNextSeriesExamDate  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						if (jsonData.get("semester") == null
								|| jsonData.get("branch") == null
								|| jsonData.get("college_id") == null) {
							responseData.put("result", false);
							responseData.put("description",
									"Please send semester ");
						} else {
							JSONObject payload = new JSONObject();
							Dbcon db = new Dbcon();

							String sql = "select min(date_milli) as date_milli from tbl_series_time_table where semester='"
									+ jsonData.get("semester")
									+ "' and branch='"
									+ jsonData.get("branch")
									+ "' and college_id='"
									+ jsonData.get("college_id") + "'";
							ResultSet rs = db.select(sql);
							if (rs.next()) {

								String examDateMilliString = rs
										.getString("date_milli");

								long examDateMilli = Long
										.parseLong(examDateMilliString);

								long oneDay = 24 * 60 * 60 * 1000;

								long noOfDaysForExam = examDateMilli
										- System.currentTimeMillis();
								System.out.println(" milli sec diff = "
										+ noOfDaysForExam);

								noOfDaysForExam = noOfDaysForExam / oneDay;

								System.out.println("No of days for exam "
										+ noOfDaysForExam);

								if (noOfDaysForExam <= 5
										&& noOfDaysForExam >= 1) {
									JSONObject resutPay = new JSONObject();
									resutPay.put("noOfDaysForExam",
											noOfDaysForExam);

									resutPay.put("title", "Series exam");
									resutPay.put("message",
											"You have next series exam in "
													+ noOfDaysForExam + " days");

									responseData.put("result", true);
									responseData.put("description",
											"Sucessfully fetched");
									responseData.put("payload", resutPay);
								} else {
									responseData.put("result", false);
									responseData.put("description",
											"No series exams are near");
								}
							} else {
								responseData.put("result", false);
								responseData.put("description",
										"No series exams");
							}
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					} catch (Exception e) {
						e.printStackTrace();
					}

					return responseData;
				});

		post("/getNextUniversityExamDate", (request, response) -> {
			System.out.println("getNextUniversityExamDate  API call "
					+ request.body() + " --- end ");
			String body = request.body();

			JSONObject responseData = new JSONObject();
			JSONParser jsonParser = new JSONParser();

			try {
				JSONObject jsonData = (JSONObject) jsonParser.parse(body);

				if (jsonData.get("semester") == null
						|| jsonData.get("branch") == null) {
					responseData.put("result", false);
					responseData.put("description", "Please send semester ");
				} else {
					JSONObject payload = new JSONObject();
					Dbcon db = new Dbcon();

					// String sql =
					// "select min(date_milli) as date_milli from tbl_series_time_table where semester='"
					// + jsonData.get("semester")
					// + "' and branch='"
					// + jsonData.get("branch")
					// + "' and college_id='"
					// + jsonData.get("college_id") + "'";

				String sql = "select min(date_milli) as date_milli from tbl_time_table where semester='"
						+ jsonData.get("semester")
						+ "' and branch='"
						+ jsonData.get("branch") + "'";

				ResultSet rs = db.select(sql);
				if (rs.next()) {

					String examDateMilliString = rs.getString("date_milli");

					long examDateMilli = Long.parseLong(examDateMilliString);

					long oneDay = 24 * 60 * 60 * 1000;

					long noOfDaysForExam = examDateMilli
							- System.currentTimeMillis();
					System.out.println(" milli sec diff = " + noOfDaysForExam);

					noOfDaysForExam = noOfDaysForExam / oneDay;

					System.out
							.println("No of days for exam " + noOfDaysForExam);

					if (noOfDaysForExam <= 5 && noOfDaysForExam >= 1) {
						JSONObject resutPay = new JSONObject();
						resutPay.put("noOfDaysForExam", noOfDaysForExam);

						resutPay.put("title", "University exam");
						resutPay.put("message",
								"You have next university exam in "
										+ noOfDaysForExam + " days");

						responseData.put("result", true);
						responseData.put("description", "Sucessfully fetched");
						responseData.put("payload", resutPay);
					} else {
						responseData.put("result", false);
						responseData.put("description",
								"No university exams are near");
					}
				} else {
					responseData.put("result", false);
					responseData.put("description", "No university exams");
				}
			}
		} catch (ParseException pe) {
			System.out.println("Error in parseing json data");
			System.out.println(pe);
			responseData.put("result", false);
			responseData.put("description", "Please send a valid json");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return responseData;
	}	);

		post("/getSeriesTimeTable",
				(request, response) -> {
					System.out.println("getSeriesTimeTable  API call "
							+ request.body() + " --- end ");
					String body = request.body();

					JSONObject responseData = new JSONObject();
					JSONParser jsonParser = new JSONParser();

					try {
						JSONObject jsonData = (JSONObject) jsonParser
								.parse(body);

						if (jsonData.get("semester") == null
								|| jsonData.get("branch") == null
								|| jsonData.get("college_id") == null) {
							responseData.put("result", false);
							responseData.put("description",
									"Please send semester ");
						} else {
							JSONObject payload = new JSONObject();
							JSONArray dataarray = new JSONArray();
							Dbcon db = new Dbcon();

							String sql = "select * from tbl_series_time_table where semester='"
									+ jsonData.get("semester")
									+ "' and branch='"
									+ jsonData.get("branch")
									+ "' and college_id='"
									+ jsonData.get("college_id") + "'";
							ResultSet rs = db.select(sql);
							while (rs.next()) {
								JSONObject eachExam = new JSONObject();

								eachExam.put("subject_name",
										rs.getString("subject_name"));

								eachExam.put("date_milli",
										rs.getString("date_milli"));

								eachExam.put("time", rs.getString("time"));

								eachExam.put("date", rs.getString("date"));

								dataarray.add(eachExam);
							}
							responseData.put("result", true);
							responseData.put("description",
									"Sucessfully fetched ");
							responseData.put("payload", dataarray);
						}
					} catch (ParseException pe) {
						System.out.println("Error in parseing json data");
						System.out.println(pe);
						responseData.put("result", false);
						responseData.put("description",
								"Please send a valid json");
					}

					return responseData;
				});

	}
}