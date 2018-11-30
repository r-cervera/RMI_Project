/**
 * Created by sbp5 on 24/10/18.
 */
package Client;

import java.io.IOException;
import java.rmi.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileOutputStream;

import CallBack.*;
import Objects.FileObject;
import Objects.Type;
import RemoteObject.*;

public class Client {

	public String msg;
	static int RMIPort = 8999;
	static String hostName = "localhost";
	static Garage h;
	public String state ="disconnected";
	private String userName = "";

	private Client()
	{
	}

	public static void main (String args[])
	{
		Client client = new Client();
		Scanner scanner = new Scanner(System.in);
		int portNum = 8001;
		client.state = "disconnected";

        try
		{
			ClientCallbackInterface callbackObj = new CallbackImpl();
			String registryURL = "rmi://"+ hostName +":" + portNum + "/some";
			Garage h = (Garage)Naming.lookup(registryURL);

			h.addCallback (callbackObj);

			while(true){
				if(client.state.equals("disconnected")){
					System.out.print("Si voleu fer registrar-vos escriviu: registrar. \n" +
							"Si voleu fer registrar-vos escriviu: logear.\n ");
					String resposta = scanner.next();

					if(resposta.toLowerCase().equals("registrar")){
						client.registrar(h);

					}else if(resposta.toLowerCase().equals("logear")){
						if(client.logear(h)){
							client.state = "connected";
						}
					}
				}else{

                System.out.print("Function over server? (deslogear ,upload, search,download) \n");
                String order = scanner.next();

				if(order.toLowerCase().equals("deslogear")){
					client.state = "disconnected";
				}

                //UPLOAD
                if(order.toLowerCase().equals("upload")){
					client.upload();
                }
                //SEARCH
				if(order.toLowerCase().equals("search")){
					System.out.print("Enter some key text: \n");
					String keyText = scanner.next();
					System.out.println(h.searchFile(keyText));
				}

                //DOWLOAD
                if(order.toLowerCase().equals("download"))
                {
					System.out.println("Enter the file title:\n");
					int id = Integer.parseInt(scanner.next());
					FileObject file = h.downloadFile(id);

					String home = System.getProperty("user.home");
					try {
						FileOutputStream fos = new FileOutputStream(home+"/Downloads/" + file.getFileName());
						fos.write(file.getFile());
					}catch(Exception e){
						System.out.println("Error downloading: " + e.toString() + "\n");
					}
				}
                //DELETE
				}
                

			}
		}

		catch (Exception e)
		{
		    System.out.println("Exception in SomeClient: " + e.toString());
		}
	}

	public void upload(){
		///home/s/sbp5/Escritorio/50mb.jpg
		Scanner scanner = new Scanner(System.in);
		FileObject fileObject = new FileObject();
		fileObject.setUser(this.userName);
		//FILE
		System.out.print("Enter the file path (E.g.: /home/s/sbp5/Escritorio/image.png): \n");
		String filePath = scanner.next();
		Path fileLocation = Paths.get(filePath);
		byte[] data = new byte[0];

		try {
			data = Files.readAllBytes(fileLocation);
		} catch (IOException e) {
			e.printStackTrace();
		}

		fileObject.setFile(data);

		//FILE NAME
		System.out.print("Enter the file name (E.g.: Pug.jpg): \n");
		String fileName = scanner.next();
		fileObject.setFileName(fileName);

		//TYPE
		System.out.print("Enter a Type MOVIE, IMAGE, TEXT, PDF or AUDIO (E.g.: image): \n");
		String t = scanner.next();
		Type type  = Type.fromString(t);
		fileObject.setType(type);

		//DESCRIPTION
		System.out.print("Enter description tags (E.g.: animal,cute,dog,love,pug) (Without spaces): \n");
		String desc = scanner.next();
		String[] keyWords = desc.split(",");
		ArrayList<String> description = new ArrayList<>();
		for(int i =0; i<keyWords.length; i++){
			description.add(keyWords[i]);
		}
		fileObject.setDescription(description);

		//IS PUBLIC
		System.out.print("You will make it public? (YES/NO): \n");
		String resp = scanner.next().toLowerCase();
		if(resp.equals("yes")){
			fileObject.setState(true);
		}else{fileObject.setState(false);}

		String response = "Error";

		System.out.println(fileObject.getFileName());
		System.out.println(fileObject.getDescription());
		System.out.println(fileObject.getState());
		System.out.println(fileObject.getType());
		System.out.println(fileObject.getUser());

		try {
			response = this.h.uploadFile(fileObject);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		System.out.println(response);
	}

	public boolean logear(Garage h){
		Scanner scanner = new Scanner(System.in);
		System.out.print("Nom de usuari:\n");
		userName = scanner.next().toLowerCase();
		System.out.print("Contrasenya:\n");
		String contrasenya = scanner.next();

		try {


			if (!contrasenya.equals("") && !userName.equals("")){
				//les contrasenyes son iguals
				boolean resposta_servidor = h.user_login(userName, contrasenya);
				if (resposta_servidor == true) {
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
			System.out.println("Exception in logear: " + e.toString());
		}
		return false;

	}
	public void registrar(Garage h){
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter a user name:\n");
		String newUserName = scanner.next().toLowerCase();
		System.out.print("Enter a password:\n");
		String password_1 = scanner.next();
		System.out.print("Repeat password:\n");
		String password_2 = scanner.next();

		try {
			if (password_1.equals(password_2)) {
				//les contrasenyes son iguals
				boolean server_response = h.user_signup(newUserName, password_1);
                System.out.println(server_response);
				if (server_response == true) {
					System.out.print("T'has registrat correctament!!!\n ja pots logear\n");
				} else {
					System.out.print("El nom de usuari no es valid! prova amb unaltre!\n");
				}

			} else {
				System.out.print("Les contrasenyes son diferents!!\n");
			}


		}
		catch (Exception e)
		{
			System.out.println("Exception in registrar: " + e.toString());
		}
	}
}
