CREATE TABLE users(
	id int primary key auto_increment not null,
    image mediumblob ,
    username varchar(20) unique not null,
    password varchar(100) not null,
    visible boolean default 1,
    rol varchar(10) not null
);

CREATE TABLE personal_details(
	id int primary key auto_increment not null,
    name varchar(45) not null,
    lastname varchar(45) not null,
    age  tinyint not null,
    email varchar(45) unique not null,
    associate_user int not null,
    foreign key (associate_user) references users(id) on delete cascade
);

CREATE TABLE publicated_images(
	id int primary key auto_increment not null,
    img mediumblob not null,
    user_owner int not null,
    created_at timestamp not null,
    description varchar(100),
    foreign key (user_owner) references users(id) on delete cascade
);
CREATE TABLE chats(
	id int primary key auto_increment not null,
    name varchar(20),
    image blob,
    type varchar(20) not null -- can be group or private
);
CREATE TABLE messages(
	id int primary key auto_increment not null,
    body varchar(100) not null,
    user_owner int not null,
    chat int not null,
    sended_at datetime not null,
    foreign key (chat) references chats(id) on delete cascade,
    foreign key (user_owner) references users(id) -- if the user is deleted i want to keep its messages, for context
);

CREATE TABLE chats_users(
	associate_user int not null,
    chat int not null,
	primary key (associate_user,chat),
	foreign key (associate_user) references users(id) on delete cascade,
    foreign key (chat) references chats(id) on delete cascade
);
CREATE TABLE  comments(
	id int primary key auto_increment not null,
    body varchar(100) not null,
    associate_user int not null,
    img int not null,
    parent_id int,
    created_at datetime not null,
    foreign key (img) references publicated_images(id) on delete cascade,
    foreign key (associate_user) references users(id) -- I want that the comments exist even if the user not, for context
);

CREATE TABLE chats_admins(
	associate_user int not null,
    chat int not null,
    primary key (associate_user,chat),
	foreign key (associate_user) references users(id) on delete cascade,
    foreign key (chat) references chats(id) on delete cascade
);

CREATE TABLE followers(
    followed int not null,
    follower int not null,
    status varchar(20) not null,
    primary key (followed,follower),
    foreign key (followed) references users(id) on delete cascade,
    foreign key (follower) references users(id) on delete cascade
);

CREATE TABLE likes(
	id int primary key not null auto_increment,
    item varchar(45) not null,
    item_id int not null, -- no fk
    decision boolean not null, -- false = dislike, true = like
    owner_like int not null,
    liked_at datetime not null,
    foreign key (owner_like) references users(id) on delete cascade
);

CREATE TABLE invalid_tokens(
	id int primary key auto_increment,
    token varchar(300),
    invalidate_date timestamp not null
);