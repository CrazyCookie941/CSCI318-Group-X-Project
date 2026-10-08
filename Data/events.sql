BEGIN TRANSACTION;
DROP TABLE IF EXISTS "events";
CREATE TABLE IF NOT EXISTS "events" (
	"id"	INTEGER,
	"title"	TEXT NOT NULL,
	"type"	TEXT NOT NULL,
	"location"	TEXT NOT NULL,
	"price"	REAL NOT NULL,
	"size"	TEXT NOT NULL,
	"description"	TEXT,
	"image_location"	TEXT,
	PRIMARY KEY("id" AUTOINCREMENT)
);
INSERT INTO "events" ("id","title","type","location","price","size","description","image_location") VALUES (1,'Radiohead concert','Concert','Afterpay Arena',50.0,'Large','Radiohead''s latest tour to Australia starts in Sydney.','/Images/394f75e3cb710d620129ea9fef2754f83b5bc54b8e8f4ffcfc2badd46c297059.jpeg');
INSERT INTO "events" ("id","title","type","location","price","size","description","image_location") VALUES (2,'Nightclub','Nightclub','Wollongong Central',35.0,'Large','An 18+ nightclub with loud DJ music and alcohol.','/Images/Wikipedia_space_ibiza(03).jpg');
INSERT INTO "events" ("id","title","type","location","price","size","description","image_location") VALUES (3,'Community barbecue','Community','Sydney Central Park',15.0,'Medium','Fun lunch for family and friends. All food and refreshments are included with your ticket price entry.','/Images/park-art-P1020661.jpg');
INSERT INTO "events" ("id","title","type","location","price","size","description","image_location") VALUES (4,'Bingo night','Games','Wollongong Community Hall',0.0,'Small','A classic bingo night for everyone to enjoy.','/Images/Bingo_night.jpg');
COMMIT;
