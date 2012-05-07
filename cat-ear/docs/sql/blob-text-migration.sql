create table audit_generic_data(audit_id bigint primary key, audit_data text);
insert into audit_generic_data(audit_id,audit_data) (select audit_id, audit_data from audit_generic);
alter table audit_generic drop column audit_data;