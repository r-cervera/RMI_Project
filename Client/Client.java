/**
 * Created by sbp5 on 24/10/18.
 */
package Client;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.rmi.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutionException;

import CallBack.*;
import GUI.ClientGUI;
import Objects.FileObject;
import Objects.Type;
import RemoteObject.*;

import javax.print.DocFlavor;

public class Client {

	public String msg;
	static int RMIPort = 8001;
	static String hostName = "localhost";//AQUEST A DE SER EL DEL SERVIDOR!!"172.16.0.26";//
	static Garage h;
	public String state ="disconnected";
	private String userName = "";
	public int callbackid;
	public ClientCallbackInterface callbackObj;


	public String clientIP;
	public String clientPORT;
	public String serverIP;
	public String serverPORT;

	private Client(){}

	//region<MAIN>
	public static void main (String args[])
	{
		Client client = new Client();
		ClientGUI.client = client;
		ClientGUI.animation();
		Scanner scanner = new Scanner(System.in);
		int portNum = 8001;//AQUEST A DE SER EL DEL SERVIDOR!!
		client.state = "disconnected";
	}
	//endregion

    public ArrayList<FileObject> getFilesByText(String text){
        ArrayList<FileObject> array = new ArrayList<>();
		try {
            array = this.h.searchFile(text, this.userName);
        }catch (Exception e)
        {
            System.out.println("Exception in Client-getFilesByText(): " + e.toString());
        }
        return array;
    }

    public ArrayList<FileObject> getFilesByUser(){
        ArrayList<FileObject> array = new ArrayList<>();
		try {
            array = this.h.searchFileByName(this.userName);
        }catch (Exception e)
        {
            System.out.println("Exception in Client-getFilesByUser(): " + e.toString());
        }
        return array;
    }

    public boolean changePassword(String oldPassword,String newPassword1){
		try{
			return this.h.changePaswordOnServer(this.userName,oldPassword,newPassword1);
		}catch (Exception e)
		{
			System.out.println("Exception in Client-changePassword(): " + e.toString());
		}
		return false;
	}

	public void deslogear(){
		try{
			this.h.deleteCallback(this.callbackid);
			this.state = "disconnected";
			this.userName = "";
		}catch (Exception e)
		{
			System.out.println("Exception in Client-deslogear(): " + e.toString());
		}
	}
	public void deleteCallbackFromClienth(){
		try{
			this.h.deleteCallback(this.callbackid);
		}catch (Exception e)
		{
			System.out.println("Exception in Client-deleteCallbackFromClient: " + e.toString());
		}

	}

	public void setUpConnections(){
		String registryURL = "rmi://"+ hostName +":" + this.serverPORT + "/some";
		try {
			this.h = (Garage)Naming.lookup(registryURL);
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		System.out.println("Garage created!");

		try {
			this.callbackObj = new CallbackImpl();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	//region<LogIn>
	//region<SignUp>
	public boolean subscribeToTag(String newTag){
		try {
			return this.h.addSubscriptionTag(this.userName, newTag.toLowerCase());
		}
		catch (Exception e)
		{
			System.out.println("Exception in Client-subscribeToTag(): " + e.toString());
		}
		return false;
	}

	public boolean desSubscribeToTag(String oldTag){
		try {
			return this.h.deleteSubscriptionTag(this.userName, oldTag.toLowerCase());
		}
		catch (Exception e)
		{
			System.out.println("Exception in subscription: " + e.toString());
		}
		return false;
	}

	public boolean logear(String username, String password){
		this.userName=username;
		String contrasenya=password;
		int callbackid = -1;
		try {
			if (!contrasenya.equals("") && !this.userName.equals("")){
				//les contrasenyes son iguals
				callbackid = this.h.user_login(this.userName, contrasenya,callbackObj);
				if (callbackid != -1) {
					this.callbackid = callbackid;
					System.out.print("T'as logeat correctamen!!!\n");
                    return true;
				} else {
					System.out.print("El nom de usuari o la contrasenya no coincideixen!\n");
				}
			} else {
				System.out.print("Un o mes camps son buit!!\n");
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception in Client-logear(): " + e.toString());
		}
		return false;

	}
	//endregion

	//region<SignIn>
	public void signin(){

	}

	public boolean registrar(String newUserName, String password_1, String password_2){
		try {
			if (password_1.equals(password_2)) {
				//les contrasenyes son iguals
				boolean server_response = this.h.user_signup(newUserName, password_1);
                System.out.println(server_response);
				if (server_response == true) {
					System.out.print("T'has registrat correctament!!!\n ja pots logear\n");
					this.userName = newUserName;
					return true;
				} else {
					System.out.print("El nom de usuari no es valid! prova amb unaltre!\n");
					return false;
				}

			} else {
				System.out.print("Les contrasenyes son diferents!!\n");
				return false;
			}
		}
		catch (Exception e)
		{
			System.out.println("Exception in Client-registrar(): " + e.toString());
			return false;
		}
	}
	//endregion
	//endregion

	//region<MODIFY Params>
	//region<Files>

	//region<Upload>
	public boolean upload(File newFile, String filename, String type, String description, ArrayList<String> tags, boolean state){

		FileObject fileObject = new FileObject();
		fileObject.setUser(this.userName);

		byte[] data = new byte[0];

		try {
			Path fileLocation = Paths.get(newFile.getAbsolutePath());
			data = Files.readAllBytes(fileLocation);

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		fileObject.setFile(data);

		//FILE NAME
		fileObject.setFileName(filename);

		//TYPE
		Type type1  = Type.fromString(type);
		fileObject.setType(type1);

		//TAGS
		fileObject.setTags(tags);

        //DESCRIPTION TEXT
        fileObject.setDescription(description);

		//IS PUBLIC
		fileObject.setState(state);

		try {
			return h.uploadFile(fileObject);
		} catch (IOException e) {
			return false;
		}

	}
	//endregion

	//region<Delete>
	public void deleteFile(){
		try{
			//return this.h.deleteFile(fileId, this.userName);
		}catch (Exception e)
		{
			System.out.println("Exception in Client-deleteFile(): " + e.toString());
		}
	}
	//endregion

	public void changeName(FileObject file, String newName){
		try {
			file.setFileName(newName);
			this.h.modifiedFile(file);
		}catch (Exception e){
			System.out.println("Exception in Client-changeName(): " + e.toString());
		}
	}

	public void changeType(FileObject file, String t){
		try {
			Type type  = Type.fromString(t);
			file.setType(type);
			this.h.modifiedFile(file);
		}catch (Exception e){
			System.out.println("Exception in Client-changeType(): " + e.toString());
		}
	}

	public String changeState(FileObject file){
		Scanner scanner = new Scanner(System.in);
		String response;

		System.out.printf("Current file state is ");
		if(file.getState()){
			System.out.printf("PUBLIC\nWill you change it to PRIVATE? (YES/NO)");
			response = scanner.next();
			if(response.toLowerCase().equals("yes")){
				file.setState(false);
				return "State changed, now this file is PRIVATE.";
			}else{return "State not changed.";}
		} else {
			System.out.printf("PRIVATE\nWill you change it to PUBLIC? (YES/NO)");
			response = scanner.next();
			if(response.toLowerCase().equals("yes")){
				file.setState(true);
				return "State changed, now this file is PUBLIC.";
			}else{return "State not changed.";}
		}
	}

	//region<Tags>
	public ArrayList<String> getSubscriptionsClient()throws RemoteException{
		System.out.println(h.toString());
		ArrayList<String> array = new ArrayList<>();
        try{
            array = h.getSubscriptionsList(this.userName);
        }catch(Exception e){
            System.out.println("Error in Client-getSubscriptionsClient(): " + e.toString());
        }
        return array;
	}

	public String addTag(FileObject file) throws RemoteException{
		Scanner scanner = new Scanner(System.in);

		System.out.println("Type a tag to add: ");
		String tag = scanner.next();

		ArrayList<String> current_description = file.getTags();
		current_description.add(tag);
		file.setTags(current_description);
		this.h.modifiedFile(file);
		return "added!";
	}

	public String removeTag(FileObject file) throws RemoteException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Type a tag to remove: ");
        String tag = scanner.next();

        ArrayList<String> current_description = file.getTags();
        if (current_description.contains(tag.toLowerCase())) {
            current_description.remove(tag);
            file.setTags(current_description);
            this.h.modifiedFile(file);
            return "removed!";
        } else {
            return "not in current tags!";
        }
    }
	//endregion

	//endregion

	//region<Users>
	//endregion
	//endregion

	public boolean checkUsername(String userName) throws RemoteException{
		try {
			return this.h.checkAvailableUser(userName);
		}catch (Exception e){
			System.out.println("Error in Client-checkUsername(): " + e.toString());
		}
		return false;
	}

	public boolean logIn(String userName, String password) throws RemoteException{
		int callbackid = -1;
		if (!password.equals("") && !userName.equals("")){
			callbackid = this.h.user_login(userName, password, callbackObj);
			if (callbackid != -1) {
				this.callbackid = callbackid;
				return true;
			}
		}
		return false;
	}
}
