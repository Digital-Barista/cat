delete from campaign_admin.users;
delete from campaign_admin.client;
delete from campaign_admin.roles;

insert into campaign_admin.users(username,password,active) 
	values ('admin','admin',1);
insert into campaign_admin.users(username,password,active) 
	values ('client1','client1',1);

insert into campaign_admin.client(name)
	values ('Test client1');
	
insert into campaign_admin.roles(username,role_name,role_type) 
	values ('admin','admin','user_type');
insert into campaign_admin.roles(username,role_name,role_type,ref_id)
	values ('client1','client','client_access',(select client_id from campaign_admin.client where name='Test client1'));
	
insert into entry_points(description,entry_point,entry_type,restriction_type) 
	values ('Email 1 Test entry point','cat1@digitalbarista.com','Email','Shared');
insert into entry_points(description,entry_point,entry_type,restriction_type)
	values ('Email 2 Test entry point','cat2@digitalbarista.com','Email','Shared');
insert into entry_points(description,entry_point,entry_type,restriction_type)
	values ('Test SMS entry point - TMobile','15032909583','SMS','Shared');
	
insert into client_entry_point_link(client_id,entry_point_id) values (1,1);
insert into client_entry_point_link(client_id,entry_point_id) values (1,2);
insert into client_entry_point_link(client_id,entry_point_id) values (1,3);