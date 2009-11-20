delete from campaign_admin.users;
delete from campaign_admin.roles;
delete from campaign_admin.client_entry_point_link;
delete from campaign_admin.client;
delete from campaign_admin.entry_points;

insert into campaign_admin.users(username,password,active) 
	values ('admin','admin',1);
insert into campaign_admin.users(username,password,active) 
	values ('client1','client1',1);

insert into campaign_admin.client(client_id, name)
	values (1, 'Test client1');

insert into campaign_admin.roles(username,role_name,role_type)
	values ('admin','admin','user_type');
insert into campaign_admin.roles(username,role_name,role_type,ref_id)
	values ('client1','client','client_access',(select client_id from campaign_admin.client where name='Test client1'));
	
insert into campaign_admin.entry_points(entry_point_id, description,entry_point,entry_type,restriction_type)
	values (1, 'Email 1 Test entry point','cat1@digitalbarista.com','Email','Shared');
insert into campaign_admin.entry_points(entry_point_id, description,entry_point,entry_type,restriction_type)
	values (2, 'Email 2 Test entry point','cat2@digitalbarista.com','Email','Shared');
insert into campaign_admin.entry_points(entry_point_id, description,entry_point,entry_type,restriction_type)
	values (3, 'Test SMS entry point - TMobile','15032909583','SMS','Shared');
	
insert into campaign_admin.client_entry_point_link(client_id,entry_point_id) values (1,1);
insert into campaign_admin.client_entry_point_link(client_id,entry_point_id) values (1,2);
insert into campaign_admin.client_entry_point_link(client_id,entry_point_id) values (1,3);