package com.falconit.joyform.client.application.login;

/*
 * #%L
 * GwtMaterial
 * %%
 * Copyright (C) 2015 - 2016 GwtMaterialDesign
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.falconit.joyform.client.application.util.Constants;
import com.falconit.joyform.client.application.util.CookieHelper;
import com.falconit.joyform.client.application.util.jbpmclient.HumanTaskHelper;
import com.falconit.joyform.client.application.validators.EmailValidator;
import com.falconit.joyform.client.application.validators.MobileValidator;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import com.falconit.joyform.client.place.NameTokens;
import com.falconit.joyform.client.resources.MyLang;
import com.falconit.joyform.shared.jsonconvert.ObjectConverter;
import gwt.material.design.client.ui.MaterialPanel;
import gwt.material.design.client.ui.MaterialTextBox;
import gwt.material.design.incubator.client.loadingstate.AppLoadingState;
import gwt.material.design.incubator.client.loadingstate.constants.State;
import java.util.logging.Level;
import java.util.logging.Logger;



public class LoginView extends ViewImpl implements LoginPresenter.MyView {
    public interface Binder extends UiBinder<Widget, LoginView> {
    }

    public static java.util.Map<String, Object[]> userMap = null;
    private EmailValidator emailValidator = new EmailValidator();
    private MobileValidator mobileValidator = new MobileValidator();
    
    private String container = "";
    private String taskId = "";
    private String taskName = "";
    private String process = "";
    private String display = "";
    private String title = "";
    private String owner = "";
    private String ownerId = "";
    
    private boolean success = true;
    @UiField
    MaterialPanel target;
    @UiField
    AppLoadingState appLoadingState;
    @UiField
    MaterialTextBox txtusername;
    @UiField
    MaterialTextBox txtpassword;

    
    @Inject
    LoginView(Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        if( CookieHelper.getMyCookie( Constants.COOKIE_USER_ID ) != null ){
            History.newItem( NameTokens.welcome );
            Window.Location.reload( );
            return;
        }
        appLoadingState.setTarget(target);
        appLoadingState.addSuccessHandler(event -> appLoadingState.reset(target));
        appLoadingState.addErrorHandler(event -> appLoadingState.reset(target));
        
        // third action
        txtusername.addKeyUpHandler(new KeyUpHandler(){
            @Override
            public void onKeyUp( KeyUpEvent event ) {
                if( txtusername.getText().trim().isEmpty() ) return;
                
                for( char c : txtusername.getText().trim( ).toCharArray() ){
                    if( !Character.isDigit(c) ){
                        txtusername.removeValidator( mobileValidator );
                        txtusername.addValidator( emailValidator );
                        return;
                    }
                }
                
                txtusername.removeValidator(emailValidator);
                txtusername.addValidator( mobileValidator ); 
            }
        });
        //second action
        txtusername.addKeyPressHandler(new KeyPressHandler(){
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if( event.getCharCode() == 13)
                    txtpassword.setFocus( true );
            }
        });
                
        txtpassword.addKeyPressHandler(new KeyPressHandler(){
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if( event.getCharCode() == 13)
                    login( );
            }
        });
        
        String value = com.google.gwt.user.client.Window.Location.getParameter( "container" );
        if( value != null ){
            container = value;
        }
        String pro = com.google.gwt.user.client.Window.Location.getParameter( "process" );
        if( pro != null ){
            process = pro;
        }
        String id = com.google.gwt.user.client.Window.Location.getParameter( "taskId" );
        if( id != null ){
            taskId = id;
        }
        String n = com.google.gwt.user.client.Window.Location.getParameter( "taskName" );
        if( n != null ){
            taskName = n;
        }
        String t = com.google.gwt.user.client.Window.Location.getParameter( "title" );
        if( t != null ){
            title = t;
        }
        String d = com.google.gwt.user.client.Window.Location.getParameter( "display" );
        if( d != null ){
            display = d;
        }
        String o = com.google.gwt.user.client.Window.Location.getParameter( "owner" );
        if( o != null ){
            owner = o;
        }
        String oID = com.google.gwt.user.client.Window.Location.getParameter( "ownerId" );
        if( oID != null ){
            ownerId = oID;
        }
    }
    
    @UiHandler("btnLogin")
    void login(ClickEvent e) {
        
//        Scheduler.get().scheduleFixedDelay(() -> {
//            if (success ) {
//                appLoadingState.setState(State.SUCCESS, "Successfully logged in", "You are now redirected to homepage.");
//            } else {
//                appLoadingState.setState(State.ERROR, "Failed logging in", "Please check your login credentials.");
//            }
//            return false;
//        }, 2000);
        login();
    }
    
//    public interface OMapper extends ObjectMapper<Users> {}
    private void login(){
        appLoadingState.setState(State.LOADING, MyLang.LANG.msg_login(), MyLang.LANG.msg_login_wait() );
        HumanTaskHelper helper = new HumanTaskHelper();
        helper.setListener(new HumanTaskHelper.HumanTaskHelperListener() {
            @Override
            public void success( String result ) {
                
                JSONObject jsonOnlineUser = JSONParser.parseStrict( result ).isObject();
                String message = jsonOnlineUser.get("message").isString().stringValue();
                if( message.equalsIgnoreCase("Success")){
                    
                    //Window.alert(jsonOnlineUser.get("user").isObject().toString());
                    //return;
                    
                    JSONObject users = jsonOnlineUser.get("user").isObject();
                    JSONObject user = users.get( "com.falconit.automation.entity.User" ).isObject();
                    
                    try {
                        userMap = new ObjectConverter().fromJSON(user, false, false);
                        String name = userMap.get("username")[1].toString();
                        String id = userMap.get("id")[1].toString();
                        String cid = userMap.get("customerId")[1].toString( );
                        String roles = userMap.get("roles")[1].toString( );
                        
                        appLoadingState.setState( State.SUCCESS, MyLang.LANG.msg_login_success( ), "Welcome " + name );

                        CookieHelper.setMyCookie( Constants.COOKIE_USER_NAME, name );
                        CookieHelper.setMyCookie( Constants.COOKIE_USER_ID, id );
                        CookieHelper.setMyCookie( Constants.COOKIE_USER_PERSON_ID, cid );//customerId
                        CookieHelper.setMyCookie( Constants.COOKIE_USER_ROLES, roles );
                        CookieHelper.setMyCookie( Constants.COOKIE_USER_CREDENTIAL, txtpassword.getText( ) + "" );

                        
                        if( !container.isEmpty() && !process.isEmpty() ){
                            Window.Location.assign(
                                "?container=" + container
                                + "&process=" + process
                                + "&title=" + title
                                + "&taskName=" + taskName
                                + "&owner=" + owner
                                + "&ownerId=" + ownerId
                                + "&display=" + display
                                + "#taskdisplay" );
                            Window.Location.reload( );
                        }else{
                            History.newItem( NameTokens.welcome );
                            Window.Location.reload( );
                        }
                    
                    } catch (Exception ex) {
                        Window.alert( "Error " + ex.getMessage());
                        Logger.getLogger(LoginView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }else{
                    appLoadingState.setState( State.ERROR, MyLang.LANG.msg_login_fail_title( ), message );
                }
            }

            @Override
            public void fail( String message, int stage ) {
              Window.alert( message );
              appLoadingState.setState(State.ERROR, MyLang.LANG.msg_login_fail_title( ), MyLang.LANG.msg_login_fail( ));
            }
        });
        
        JSONObject users = new JSONObject( );
        String email = "";
        String mobile = "";
        //Window.alert(txtusername.getText());
        if( txtusername.getText().contains("@") || txtusername.getText().contains("."))
            email = txtusername.getText().trim( );
        else
            mobile = txtusername.getText().trim( );
        
        users.put("email", new JSONString( email ) );
        users.put("phone", new JSONString( mobile ) );
        users.put("password", new JSONString( txtpassword.getText() ));
        
        JSONObject user = new JSONObject( );
        user.put("User", users);
        
        JSONObject obj = new JSONObject( );
        obj.put("action", new JSONString( "login" ));
        obj.put("user", user);
        
        helper.startInstances( Constants.userProcessId, obj.toString() );
        //helper.query( Constants.containerId, "355");
    }
    
}
