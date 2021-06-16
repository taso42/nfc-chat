-- this is an example of setting up the required tables/views
-- JdbcIgnoreStore, JdbcAuthenticator, and JdbcRoomAuthenticator on postgresql.
-- (for the curious, the tables and functions referenced are from
--  the [open]ACS 3.x community system.)

---
--- for JdbcAuthenticator
---
drop view nfc_users;
create view nfc_users as
select replace(last_name, ' ', '_') as uname, password as password,
case when ad_group_member_p(user_id, ad_group_id('bf2')) = 't'
     then 100 -- "GOD" level NFC permissions
     else 1 -- regular user
     end as access
from users
where user_state = 'authorized'
;

---
--- for JdbcRoomAuthenticator
---
create table nfc_public_rooms (
	id					serial primary key,
	name				varchar(20)
)
;

-- first, predefined public rooms may not be passworded
-- second, only users with a bitflag set in member_p may create non-predefined rooms.
drop function nfc_is_create_allowed(varchar, varchar, varchar);
create function nfc_is_create_allowed(varchar, varchar, varchar) returns boolean as '
declare
	uname alias for $1;
	roomname alias for $2;
	passwd alias for $3;
	is_room_public boolean;
	may_create_nonpublic boolean;
begin
	is_room_public = exists (select 1 from nfc_public_rooms where lower(name) = lower(roomname));

	if (is_room_public) then
		return passwd is null;
	end if;

	may_create_nonpublic = (select member_p & 1 from users where lower(last_name) = lower(replace(uname, ''_'', '' ''))) != 0;
	return case when may_create_nonpublic is null then false else may_create_nonpublic end;
end;
' language 'plpgsql'
;

---
--- for JdbcIgnoreStore
---
create table users_ignoring_users (
    ignorer             int references users(user_id),
    ignoree             int references users(user_id),
    constraint users_ignoring_users_unique unique(ignorer, ignoree)
);

drop view nfc_users_ignoring_users;
create view nfc_users_ignoring_users as
select replace(u1.last_name, ' ', '_') as ignorer,
       replace(u2.last_name, ' ', '_') as ignoree
from users u1, users u2, users_ignoring_users uiu
where u1.user_id = uiu.ignorer
  and u2.user_id = uiu.ignoree
;

-- PostgreSQL's rule system is not standard; in most (all?) other databases
-- you will need to use triggers instead.  (PG also provides triggers,
-- but rules can often handle simple cases more easily.)
-- I have not created an update rule; insert/delete are only expected operations.

create rule nfc_users_ignoring_users_ir as
on insert to nfc_users_ignoring_users
do instead
insert into users_ignoring_users(ignorer, ignoree)
select (select user_id from users where lower(last_name) = replace(lower(NEW.ignorer), '_', ' ')),
       (select user_id from users where lower(last_name) = replace(lower(NEW.ignoree), '_', ' '))
;

-- the lower() calls here do NOT mean that case is ignored in the delete;
-- they are just there to take advantage of an existing index.
-- results would be identical w/o it, just slower.
create rule nfc_users_ignoring_users_dr as
on delete to nfc_users_ignoring_users
do instead
delete from users_ignoring_users
where ignorer = (select user_id from users where lower(last_name) = replace(lower(OLD.ignorer), '_', ' '))
  and ignoree = (select user_id from users where lower(last_name) = replace(lower(OLD.ignoree), '_', ' '))
;
