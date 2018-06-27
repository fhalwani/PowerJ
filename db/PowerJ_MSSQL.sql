USE [master]
GO
/****** Object:  Database [PowerJ]    Script Date: 03/17/2018 09:58:49 ******/
CREATE DATABASE [PowerJ] ON PRIMARY ( NAME = N'PowerJ', FILENAME = N'E:\SQL_DATA\PowerJ.mdf' , SIZE = 327680KB, MAXSIZE = UNLIMITED, FILEGROWTH = 10240KB )
LOG ON (NAME = N'PowerJ_log', FILENAME = N'F:\SQL_LOGS\PowerJ_log.ldf' , SIZE = 688384KB , MAXSIZE = 2048GB, FILEGROWTH = 10%)
GO
ALTER DATABASE [PowerJ] SET COMPATIBILITY_LEVEL = 100
GO
ALTER DATABASE [PowerJ] SET ANSI_NULL_DEFAULT OFF
GO
ALTER DATABASE [PowerJ] SET ANSI_NULLS OFF
GO
ALTER DATABASE [PowerJ] SET ANSI_PADDING OFF
GO
ALTER DATABASE [PowerJ] SET ANSI_WARNINGS OFF
GO
ALTER DATABASE [PowerJ] SET ARITHABORT OFF
GO
ALTER DATABASE [PowerJ] SET AUTO_CLOSE OFF
GO
ALTER DATABASE [PowerJ] SET AUTO_CREATE_STATISTICS ON
GO
ALTER DATABASE [PowerJ] SET AUTO_SHRINK OFF
GO
ALTER DATABASE [PowerJ] SET AUTO_UPDATE_STATISTICS ON
GO
ALTER DATABASE [PowerJ] SET CURSOR_CLOSE_ON_COMMIT OFF
GO
ALTER DATABASE [PowerJ] SET CURSOR_DEFAULT GLOBAL
GO
ALTER DATABASE [PowerJ] SET CONCAT_NULL_YIELDS_NULL OFF
GO
ALTER DATABASE [PowerJ] SET NUMERIC_ROUNDABORT OFF
GO
ALTER DATABASE [PowerJ] SET QUOTED_IDENTIFIER OFF
GO
ALTER DATABASE [PowerJ] SET RECURSIVE_TRIGGERS OFF
GO
ALTER DATABASE [PowerJ] SET  DISABLE_BROKER
GO
ALTER DATABASE [PowerJ] SET AUTO_UPDATE_STATISTICS_ASYNC OFF
GO
ALTER DATABASE [PowerJ] SET DATE_CORRELATION_OPTIMIZATION OFF
GO
ALTER DATABASE [PowerJ] SET TRUSTWORTHY OFF
GO
ALTER DATABASE [PowerJ] SET ALLOW_SNAPSHOT_ISOLATION OFF
GO
ALTER DATABASE [PowerJ] SET PARAMETERIZATION SIMPLE
GO
ALTER DATABASE [PowerJ] SET READ_COMMITTED_SNAPSHOT OFF
GO
ALTER DATABASE [PowerJ] SET HONOR_BROKER_PRIORITY OFF
GO
ALTER DATABASE [PowerJ] SET READ_WRITE
GO
ALTER DATABASE [PowerJ] SET RECOVERY SIMPLE
GO
ALTER DATABASE [PowerJ] SET MULTI_USER
GO
ALTER DATABASE [PowerJ] SET PAGE_VERIFY CHECKSUM
GO
ALTER DATABASE [PowerJ] SET DB_CHAINING OFF
GO

USE [PowerJ]
GO

/****** Users ******/
CREATE USER [PJServer] FOR LOGIN [PJServer] WITH DEFAULT_SCHEMA=[dbo]
GO
CREATE USER [PJClient] FOR LOGIN [PJClient] WITH DEFAULT_SCHEMA=[dbo]
GO

/****** Tables ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO

CREATE TABLE [dbo].[Setup](
	[STPID] [smallint] NOT NULL,
	[STPVAL] [varchar](64) NOT NULL,
PRIMARY KEY CLUSTERED ([STPID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO

CREATE TABLE [dbo].[Rules](
	[ID] [smallint] NOT NULL,
	[NAME] [varchar](32) NOT NULL,
	[DESCR] [varchar](256) NOT NULL,
PRIMARY KEY CLUSTERED ([ID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
UNIQUE NONCLUSTERED ([NAME] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO

CREATE TABLE [dbo].[Facilities](
	[FACID] [smallint] NOT NULL,
	[DASH] [char](1) NOT NULL,
	[WLOAD] [char](1) NOT NULL,
	[CODE] [varchar](4) NOT NULL,
	[NAME] [varchar](80) NOT NULL,
PRIMARY KEY CLUSTERED (FACID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO

CREATE TABLE [dbo].[Specialties](
	[SPYID] [smallint] NOT NULL,
	[DASH] [char](1) NOT NULL,
	[WLOAD] [char](1) NOT NULL,
	[CODESPEC] [char](1) NOT NULL,
	[SPYNAME] [varchar](16) NOT NULL,
PRIMARY KEY CLUSTERED ([SPYID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO

CREATE TABLE [dbo].[Personnel](
	[PERID] [smallint] NOT NULL,
	[ACCESS] [int] NOT NULL,
	[CODE] [char](2) NOT NULL,
	[INITIALS] [char](3) NOT NULL,
	[PLAST] [varchar](30) NOT NULL,
	[PFIRST] [varchar](30) NOT NULL,
PRIMARY KEY CLUSTERED ([PERID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO

CREATE TABLE [dbo].[Coder1](
	[ID] [smallint] NOT NULL,
	[RULEID] [smallint] NOT NULL,
	[COUNT] [smallint] NOT NULL,
	[VALUE1] [decimal](5, 3) NOT NULL,
	[VALUE2] [decimal](5, 3) NOT NULL,
	[VALUE3] [decimal](5, 3) NOT NULL,
	[NAME] [varchar](16) NOT NULL,
	[DESCR] [varchar](128) NOT NULL,
PRIMARY KEY CLUSTERED ([ID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
UNIQUE NONCLUSTERED ([NAME] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO
ALTER TABLE [dbo].[Coder1] WITH CHECK ADD FOREIGN KEY([RULEID]) REFERENCES [dbo].[Rules] ([ID])
GO

CREATE TABLE [dbo].[Coder2](
	[ID] [smallint] NOT NULL,
	[RULEID] [smallint] NOT NULL,
	[COUNT] [smallint] NOT NULL,
	[VALUE1] [decimal](5, 3) NOT NULL,
	[VALUE2] [decimal](5, 3) NOT NULL,
	[VALUE3] [decimal](5, 3) NOT NULL,
	[NAME] [varchar](16) NOT NULL,
	[DESCR] [varchar](128) NOT NULL,
PRIMARY KEY CLUSTERED ([ID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
UNIQUE NONCLUSTERED ([NAME] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO
ALTER TABLE [dbo].[Coder2] WITH CHECK ADD FOREIGN KEY([RULEID]) REFERENCES [dbo].[Rules] ([ID])
GO

CREATE TABLE [dbo].[Coder3](
	[ID] [smallint] NOT NULL,
	[RULEID] [smallint] NOT NULL,
	[COUNT] [smallint] NOT NULL,
	[VALUE1] [decimal](5, 3) NOT NULL,
	[VALUE2] [decimal](5, 3) NOT NULL,
	[VALUE3] [decimal](5, 3) NOT NULL,
	[NAME] [varchar](16) NOT NULL,
	[DESCR] [varchar](128) NOT NULL,
PRIMARY KEY CLUSTERED ([ID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
UNIQUE NONCLUSTERED ([NAME] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO
ALTER TABLE [dbo].[Coder3] WITH CHECK ADD FOREIGN KEY([RULEID]) REFERENCES [dbo].[Rules] ([ID])
GO

CREATE TABLE [dbo].[Coder4](
	[ID] [smallint] NOT NULL,
	[RULEID] [smallint] NOT NULL,
	[COUNT] [smallint] NOT NULL,
	[VALUE1] [decimal](5, 3) NOT NULL,
	[VALUE2] [decimal](5, 3) NOT NULL,
	[VALUE3] [decimal](5, 3) NOT NULL,
	[NAME] [varchar](16) NOT NULL,
	[DESCR] [varchar](128) NOT NULL,
PRIMARY KEY CLUSTERED ([ID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
UNIQUE NONCLUSTERED ([NAME] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO
ALTER TABLE [dbo].[Coder4] WITH CHECK ADD FOREIGN KEY([RULEID]) REFERENCES [dbo].[Rules] ([ID])
GO

CREATE TABLE [dbo].[Accessions](
	[ACCID] [smallint] NOT NULL,
	[SPYID] [smallint] NOT NULL,
	[DASH] [char](1) NOT NULL,
	[WLOAD] [char](1) NOT NULL,
	[ACCNAME] [varchar](30) NOT NULL,
PRIMARY KEY CLUSTERED ([ACCID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO
ALTER TABLE [dbo].[Accessions] WITH CHECK ADD FOREIGN KEY([SPYID]) REFERENCES [dbo].[Specialties] ([SPYID])
GO

CREATE TABLE [dbo].[Subspecial](
	[SUBID] [smallint] NOT NULL,
	[SPYID] [smallint] NOT NULL,
	[SUBINIT] [varchar](3) NOT NULL,
	[SUBNAME] [varchar](30) NOT NULL,
PRIMARY KEY CLUSTERED ([SUBID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO

ALTER TABLE [dbo].[Subspecial] WITH CHECK ADD FOREIGN KEY([SPYID]) REFERENCES [dbo].[Specialties] ([SPYID])
GO

CREATE TABLE [dbo].[Groups](
	[ID] [smallint] NOT NULL,
	[GRP] [smallint] NOT NULL,
	[CODE1] [smallint] NOT NULL,
	[CODE2] [smallint] NOT NULL,
	[CODE3] [smallint] NOT NULL,
	[CODE4] [smallint] NOT NULL,
	[NAME] [varchar](8) NOT NULL,
PRIMARY KEY CLUSTERED ([ID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
UNIQUE NONCLUSTERED ([NAME] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO
ALTER TABLE [dbo].[Groups] WITH CHECK ADD FOREIGN KEY([CODE1]) REFERENCES [dbo].[Coder1] ([ID])
GO
ALTER TABLE [dbo].[Groups] WITH CHECK ADD FOREIGN KEY([CODE2]) REFERENCES [dbo].[Coder2] ([ID])
GO
ALTER TABLE [dbo].[Groups] WITH CHECK ADD FOREIGN KEY([CODE3]) REFERENCES [dbo].[Coder3] ([ID])
GO
ALTER TABLE [dbo].[Groups] WITH CHECK ADD FOREIGN KEY([CODE4]) REFERENCES [dbo].[Coder4] ([ID])
GO

CREATE TABLE [dbo].[MasterSpecimens](
	[MSID] [smallint] NOT NULL,
	[SPYID] [smallint] NOT NULL,
	[SUBID] [smallint] NOT NULL,
	[PROCID] [smallint] NOT NULL,
	[ISLN] [smallint] NOT NULL,
	[GROSS] [smallint] NOT NULL,
	[EMBED] [smallint] NOT NULL,
	[MICROTOMY] [smallint] NOT NULL,
	[ROUTE] [smallint] NOT NULL,
	[SIGNOUT] [smallint] NOT NULL,
	[CODE1B] [smallint] NOT NULL,
	[CODE1M] [smallint] NOT NULL,
	[CODE1R] [smallint] NOT NULL,
	[CODE2B] [smallint] NOT NULL,
	[CODE2M] [smallint] NOT NULL,
	[CODE2R] [smallint] NOT NULL,
	[CODE3B] [smallint] NOT NULL,
	[CODE3M] [smallint] NOT NULL,
	[CODE3R] [smallint] NOT NULL,
	[CODE4B] [smallint] NOT NULL,
	[CODE4M] [smallint] NOT NULL,
	[CODE4R] [smallint] NOT NULL,
	[CODE] [varchar](15) NOT NULL,
	[DESCR] [varchar](80) NOT NULL,
PRIMARY KEY CLUSTERED ([MSID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
UNIQUE NONCLUSTERED ([CODE] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO
ALTER TABLE [dbo].[MasterSpecimens] WITH CHECK ADD FOREIGN KEY([SPYID]) REFERENCES [dbo].[Specialties] ([SPYID])
GO
ALTER TABLE [dbo].[MasterSpecimens] WITH CHECK ADD FOREIGN KEY([SUBID]) REFERENCES [dbo].[Subspecial] ([SUBID])
GO
ALTER TABLE [dbo].[MasterSpecimens] WITH CHECK ADD FOREIGN KEY([CODE1B]) REFERENCES [dbo].[Coder1] ([ID])
GO
ALTER TABLE [dbo].[MasterSpecimens] WITH CHECK ADD FOREIGN KEY([CODE1M]) REFERENCES [dbo].[Coder1] ([ID])
GO
ALTER TABLE [dbo].[MasterSpecimens] WITH CHECK ADD FOREIGN KEY([CODE1R]) REFERENCES [dbo].[Coder1] ([ID])
GO
ALTER TABLE [dbo].[MasterSpecimens] WITH CHECK ADD FOREIGN KEY([CODE2B]) REFERENCES [dbo].[Coder2] ([ID])
GO
ALTER TABLE [dbo].[MasterSpecimens] WITH CHECK ADD FOREIGN KEY([CODE2M]) REFERENCES [dbo].[Coder2] ([ID])
GO
ALTER TABLE [dbo].[MasterSpecimens] WITH CHECK ADD FOREIGN KEY([CODE2R]) REFERENCES [dbo].[Coder2] ([ID])
GO
ALTER TABLE [dbo].[MasterSpecimens] WITH CHECK ADD FOREIGN KEY([CODE3B]) REFERENCES [dbo].[Coder3] ([ID])
GO
ALTER TABLE [dbo].[MasterSpecimens] WITH CHECK ADD FOREIGN KEY([CODE3M]) REFERENCES [dbo].[Coder3] ([ID])
GO
ALTER TABLE [dbo].[MasterSpecimens] WITH CHECK ADD FOREIGN KEY([CODE3R]) REFERENCES [dbo].[Coder3] ([ID])
GO
ALTER TABLE [dbo].[MasterSpecimens] WITH CHECK ADD FOREIGN KEY([CODE4B]) REFERENCES [dbo].[Coder4] ([ID])
GO
ALTER TABLE [dbo].[MasterSpecimens] WITH CHECK ADD FOREIGN KEY([CODE4M]) REFERENCES [dbo].[Coder4] ([ID])
GO
ALTER TABLE [dbo].[MasterSpecimens] WITH CHECK ADD FOREIGN KEY([CODE4R]) REFERENCES [dbo].[Coder4] ([ID])
GO

CREATE TABLE [dbo].[MasterOrders](
	[ID] [smallint] NOT NULL,
	[GRPID] [smallint] NOT NULL,
	[CODE] [varchar](15) NOT NULL,
	[DESCR] [varchar](80) NOT NULL,
PRIMARY KEY CLUSTERED ([ID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
UNIQUE NONCLUSTERED ([CODE] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO
ALTER TABLE [dbo].[MasterOrders] WITH CHECK ADD FOREIGN KEY([GRPID]) REFERENCES [dbo].[Groups] ([ID])
GO

CREATE TABLE [dbo].[Errors](
	[CASEID] [int] NOT NULL,
	[ERRID] [int] NOT NULL,
	[CASENO] [char](12) NOT NULL,
	[COMMENT] [varchar](max) NOT NULL,
PRIMARY KEY CLUSTERED ([CASEID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

CREATE TABLE [dbo].[Pending](
	[CASEID] [int] NOT NULL,
	[FACID] [smallint] NOT NULL,
	[MSID] [smallint] NOT NULL,
	[GROSSTAT] [smallint] NOT NULL,
	[EMBEDTAT] [smallint] NOT NULL,
	[MICROTAT] [smallint] NOT NULL,
	[STAINTAT] [smallint] NOT NULL,
	[ROUTETAT] [smallint] NOT NULL,
	[HISTOTAT] [smallint] NOT NULL,
	[FINALTAT] [smallint] NOT NULL,
	[GROSSID] [smallint] NOT NULL,
	[EMBEDID] [smallint] NOT NULL,
	[MICROID] [smallint] NOT NULL,
	[STAINID] [smallint] NOT NULL,
	[ROUTEID] [smallint] NOT NULL,
	[FINALID] [smallint] NOT NULL,
	[SPYID] [smallint] NOT NULL,
	[SUBID] [smallint] NOT NULL,
	[PROID] [smallint] NOT NULL,
	[STATUS] [smallint] NOT NULL,
	[NOSPECS] [smallint] NOT NULL,
	[NOBLOCKS] [smallint] NOT NULL,
	[NOSLIDES] [smallint] NOT NULL,
	[ACCESSED] [datetime] NOT NULL,
	[GROSSED] [datetime] NOT NULL,
	[EMBEDED] [datetime] NOT NULL,
	[MICROED] [datetime] NOT NULL,
	[STAINED] [datetime] NOT NULL,
	[ROUTED] [datetime] NOT NULL,
	[FINALED] [datetime] NOT NULL,
	[CASENO] [char](12) NOT NULL,
PRIMARY KEY CLUSTERED ([CASEID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
UNIQUE NONCLUSTERED ([CASENO] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO
ALTER TABLE [dbo].[Pending] ADD CONSTRAINT [DF_Pending_GROSSTAT] DEFAULT ((0)) FOR [GROSSTAT]
GO
ALTER TABLE [dbo].[Pending] ADD CONSTRAINT [DF_Pending_EMBEDTAT] DEFAULT ((0)) FOR [EMBEDTAT]
GO
ALTER TABLE [dbo].[Pending] ADD CONSTRAINT [DF_Pending_MICROTAT] DEFAULT ((0)) FOR [MICROTAT]
GO
ALTER TABLE [dbo].[Pending] ADD CONSTRAINT [DF_Pending_STAINTAT] DEFAULT ((0)) FOR [STAINTAT]
GO
ALTER TABLE [dbo].[Pending] ADD CONSTRAINT [DF_Pending_ROUTETAT] DEFAULT ((0)) FOR [ROUTETAT]
GO
ALTER TABLE [dbo].[Pending] ADD CONSTRAINT [DF_Pending_HISTOTAT] DEFAULT ((0)) FOR [HISTOTAT]
GO
ALTER TABLE [dbo].[Pending] ADD CONSTRAINT [DF_Pending_FINALTAT] DEFAULT ((0)) FOR [FINALTAT]
GO
ALTER TABLE [dbo].[Pending] ADD CONSTRAINT [DF_Pending_GROSSED] DEFAULT (getdate()) FOR [GROSSED]
GO
ALTER TABLE [dbo].[Pending] ADD CONSTRAINT [DF_Pending_EMBEDED] DEFAULT (getdate()) FOR [EMBEDED]
GO
ALTER TABLE [dbo].[Pending] ADD CONSTRAINT [DF_Pending_MICROED] DEFAULT (getdate()) FOR [MICROED]
GO
ALTER TABLE [dbo].[Pending] ADD CONSTRAINT [DF_Pending_STAINED] DEFAULT (getdate()) FOR [STAINED]
GO
ALTER TABLE [dbo].[Pending] ADD CONSTRAINT [DF_Pending_ROUTED] DEFAULT (getdate()) FOR [ROUTED]
GO
ALTER TABLE [dbo].[Pending] ADD CONSTRAINT [DF_Pending_FINALED] DEFAULT (getdate()) FOR [FINALED]
GO

CREATE TABLE [dbo].[Cases](
	[CASEID] [int] NOT NULL,
	[FACID] [smallint] NOT NULL,
	[MSID] [smallint] NOT NULL,
	[GROSSID] [smallint] NOT NULL,
	[FINALID] [smallint] NOT NULL,
	[SPYID] [smallint] NOT NULL,
	[SUBID] [smallint] NOT NULL,
	[GROSSTAT] [smallint] NOT NULL,
	[ROUTETAT] [smallint] NOT NULL,
	[FINALTAT] [smallint] NOT NULL,
	[TOTALTAT] [smallint] NOT NULL,
	[PROID] [smallint] NOT NULL,
	[NOSPECS] [smallint] NOT NULL,
	[NOBLOCKS] [smallint] NOT NULL,
	[NOSLIDES] [smallint] NOT NULL,
	[NOSYNOPT] [smallint] NOT NULL,
	[NOFS] [smallint] NOT NULL,
	[ACCESSED] [date] NOT NULL,
	[GROSSED] [date] NOT NULL,
	[ROUTED] [date] NOT NULL,
	[FINALED] [datetime] NOT NULL,
	[VALUE1] [decimal](5, 3) NOT NULL,
	[VALUE2] [decimal](5, 3) NOT NULL,
	[VALUE3] [decimal](5, 3) NOT NULL,
	[VALUE4] [decimal](5, 3) NOT NULL,
	[CASENO] [char](12) NOT NULL,
PRIMARY KEY CLUSTERED ([CASEID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
UNIQUE NONCLUSTERED ([CASENO] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO
ALTER TABLE [dbo].[Cases] ADD DEFAULT (getdate()) FOR [FINALED]
GO
ALTER TABLE [dbo].[Cases] WITH CHECK ADD CONSTRAINT [Cases_Facilities] FOREIGN KEY([FACID]) REFERENCES [dbo].[Facilities] ([FACID])
GO
ALTER TABLE [dbo].[Cases] WITH CHECK ADD CONSTRAINT [Cases_MasterSpec] FOREIGN KEY([MSID]) REFERENCES [dbo].[MasterSpecimens] ([MSID])
GO
ALTER TABLE [dbo].[Cases] WITH CHECK ADD CONSTRAINT [Cases_Specialties] FOREIGN KEY([SPYID]) REFERENCES [dbo].[Specialties] ([SPYID])
GO
ALTER TABLE [dbo].[Cases]  WITH CHECK ADD CONSTRAINT [Cases_SubSpecial] FOREIGN KEY([SUBID]) REFERENCES [dbo].[Subspecial] ([SUBID])
GO
ALTER TABLE [dbo].[Cases] WITH CHECK ADD CONSTRAINT [Cases_GrossID] FOREIGN KEY([GROSSID]) REFERENCES [dbo].[Personnel] ([PERID])
GO
ALTER TABLE [dbo].[Cases] WITH CHECK ADD CONSTRAINT [Cases_FinalID] FOREIGN KEY([FINALID]) REFERENCES [dbo].[Personnel] ([PERID])
GO
CREATE NONCLUSTERED INDEX [Cases_Finaled] ON [dbo].[Cases] ([FINALED] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

CREATE TABLE [dbo].[Specimens](
	[SPECID] [int] NOT NULL,
	[CASEID] [int] NOT NULL,
	[MSID] [smallint] NOT NULL,
	[NOBLOCKS] [smallint] NOT NULL,
	[NOSLIDES] [smallint] NOT NULL,
	[VALUE1] [decimal](5, 3) NOT NULL,
	[VALUE2] [decimal](5, 3) NOT NULL,
	[VALUE3] [decimal](5, 3) NOT NULL,
	[VALUE4] [decimal](5, 3) NOT NULL,
	[DESCR] [varchar](64) NOT NULL,
	[NOFRAGS] [smallint] NOT NULL,
PRIMARY KEY CLUSTERED ([SPECID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO
ALTER TABLE [dbo].[Specimens] WITH CHECK ADD FOREIGN KEY([CASEID]) REFERENCES [dbo].[Cases] ([CASEID])
GO
ALTER TABLE [dbo].[Specimens] WITH CHECK ADD FOREIGN KEY([MSID]) REFERENCES [dbo].[MasterSpecimens] ([MSID])
GO

CREATE TABLE [dbo].[Orders](
	[SPECID] [int] NOT NULL,
	[GRPID] [smallint] NOT NULL,
	[QTY] [smallint] NOT NULL,
	[VALUE1] [decimal](5, 3) NOT NULL,
	[VALUE2] [decimal](5, 3) NOT NULL,
	[VALUE3] [decimal](5, 3) NOT NULL,
	[VALUE4] [decimal](5, 3) NOT NULL,
PRIMARY KEY CLUSTERED ([SPECID] ASC, [GRPID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO
ALTER TABLE [dbo].[Orders] WITH CHECK ADD FOREIGN KEY([SPECID]) REFERENCES [dbo].[Specimens] ([SPECID])
GO
ALTER TABLE [dbo].[Orders] WITH CHECK ADD FOREIGN KEY([GRPID]) REFERENCES [dbo].[Groups] ([ID])
GO

CREATE TABLE [dbo].[Frozens](
	[CASEID] [int] NOT NULL,
	[PERID] [smallint] NOT NULL,
	[NOSPECS] [smallint] NOT NULL,
	[NOBLOCKS] [smallint] NOT NULL,
	[NOSLIDES] [smallint] NOT NULL,
	[VALUE1] [decimal](5, 3) NOT NULL,
	[VALUE2] [decimal](5, 3) NOT NULL,
	[VALUE3] [decimal](5, 3) NOT NULL,
	[VALUE4] [decimal](5, 3) NOT NULL,
PRIMARY KEY CLUSTERED ([CASEID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO
ALTER TABLE [dbo].[Frozens] WITH CHECK ADD FOREIGN KEY([CASEID]) REFERENCES [dbo].[Cases] ([CASEID])
GO
ALTER TABLE [dbo].[Frozens] WITH CHECK ADD FOREIGN KEY([PERID]) REFERENCES [dbo].[Personnel] ([PERID])
GO

CREATE TABLE [dbo].[Additional](
	[CASEID] [int] NOT NULL,
	[PERID] [smallint] NOT NULL,
	[CODEID] [smallint] NOT NULL,
	[FINALED] [date] NOT NULL,
	[VALUE1] [decimal](5, 3) NOT NULL,
	[VALUE2] [decimal](5, 3) NOT NULL,
	[VALUE3] [decimal](5, 3) NOT NULL,
	[VALUE4] [decimal](5, 3) NOT NULL,
PRIMARY KEY CLUSTERED ([CASEID] ASC, [PERID] ASC, [CODEID] ASC, [FINALED] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO
ALTER TABLE [dbo].[Additional] WITH CHECK ADD FOREIGN KEY([CASEID]) REFERENCES [dbo].[Cases] ([CASEID])
GO
ALTER TABLE [dbo].[Additional] WITH CHECK ADD FOREIGN KEY([PERID]) REFERENCES [dbo].[Personnel] ([PERID])
GO
CREATE NONCLUSTERED INDEX [Additional_Finaled] ON [dbo].[Additional] ([FINALED] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX [Additional_Personnel] ON [dbo].[Additional] ([PERID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

CREATE TABLE [dbo].[Comments](
	[CASEID] [int] NOT NULL,
	[COMMENT] [varchar](max) NOT NULL,
PRIMARY KEY CLUSTERED ([CASEID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
ALTER TABLE [dbo].[Comments] WITH CHECK ADD FOREIGN KEY([CASEID]) REFERENCES [dbo].[Cases] ([CASEID])
GO

CREATE TABLE [dbo].[Stats](
	[CASEID] [int] NOT NULL,
	[FACID] [smallint] NOT NULL,
	[GROSSID] [smallint] NOT NULL,
	[FINALID] [smallint] NOT NULL,
	[SPYID] [smallint] NOT NULL,
	[SUBID] [smallint] NOT NULL,
	[PROID] [smallint] NOT NULL,
	[NOSPECS] [smallint] NOT NULL,
	[NOBLOCKS] [smallint] NOT NULL,
	[NOSLIDES] [smallint] NOT NULL,
	[NOHE] [smallint] NOT NULL,
	[NOSS] [smallint] NOT NULL,
	[NOIHC] [smallint] NOT NULL,
	[NOMOL] [smallint] NOT NULL,
	[NOFSP] [smallint] NOT NULL,
	[NOFBL] [smallint] NOT NULL,
	[NOFSL] [smallint] NOT NULL,
	[NOSYN] [smallint] NOT NULL,
	[GRTAT] [smallint] NOT NULL,
	[ROTAT] [smallint] NOT NULL,
	[FITAT] [smallint] NOT NULL,
	[TOTAT] [smallint] NOT NULL,
	[MSID] [smallint] NOT NULL,
	[ACCESSED] [datetime] NOT NULL,
	[GROSSED] [datetime] NOT NULL,
	[ROUTED] [datetime] NOT NULL,
	[FINALED] [datetime] NOT NULL,
	[CASENO] [char](12) NOT NULL,
PRIMARY KEY CLUSTERED ([CASEID] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY],
UNIQUE NONCLUSTERED ([CASENO] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]) 
ON [PRIMARY]
GO
ALTER TABLE [dbo].[Stats] WITH CHECK ADD FOREIGN KEY([FACID]) REFERENCES [dbo].[Facilities] ([FACID])
GO
ALTER TABLE [dbo].[Stats] WITH CHECK ADD FOREIGN KEY([FINALID]) REFERENCES [dbo].[Personnel] ([PERID])
GO
ALTER TABLE [dbo].[Stats] WITH CHECK ADD FOREIGN KEY([GROSSID]) REFERENCES [dbo].[Personnel] ([PERID])
GO
ALTER TABLE [dbo].[Stats] WITH CHECK ADD FOREIGN KEY([MSID]) REFERENCES [dbo].[MasterSpecimens] ([MSID])
GO
ALTER TABLE [dbo].[Stats] WITH CHECK ADD FOREIGN KEY([SPYID]) REFERENCES [dbo].[Specialties] ([SPYID])
GO
ALTER TABLE [dbo].[Stats] WITH CHECK ADD FOREIGN KEY([SUBID]) REFERENCES [dbo].[Subspecial] ([SUBID])
GO
CREATE NONCLUSTERED INDEX [Stats_Finaled] ON [dbo].[Stats] ([FINALED] ASC) WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
GO

-- -----------------------------------------------------
-- Create Views
-- -----------------------------------------------------

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE VIEW [dbo].[udvAccessions]
AS
	SELECT a.ACCID, a.SPYID, a.DASH, a.WLOAD, a.ACCNAME,
		s.CODESPEC, s.DASH AS SDASH, s.WLOAD AS SWLOAD, s.SPYNAME
	FROM dbo.Accessions AS a
	INNER JOIN dbo.Specialties AS s ON a.SPYID = s.SPYID;
GO

CREATE VIEW [dbo].[udvAdditional]
AS
SELECT a.CASEID, c.FACID, c.SPYID, c.SUBID, a.PERID,
       a.CODEID, a.VALUE1, a.VALUE2, a.VALUE3, a.VALUE4,
       a.FINALED, c.CASENO, f.CODE, b.SUBINIT, p.INITIALS, s.SPYNAME
FROM   dbo.Additional AS a
       INNER JOIN dbo.Cases AS c ON c.CASEID = a.CASEID
       INNER JOIN dbo.Facilities AS f ON f.FACID = c.FACID
       INNER JOIN dbo.Personnel AS p ON p.PERID = a.PERID
       INNER JOIN dbo.Specialties AS s ON s.SPYID = c.SPYID
       INNER JOIN dbo.Subspecial AS b ON b.SUBID = c.SUBID
GO

CREATE VIEW [dbo].[udvCases]
AS
	SELECT c.CASEID, c.FACID, c.SPYID, c.SUBID, c.FINALID, c.PROID,
		c.NOSYNOPT, c.NOSPECS, c.NOBLOCKS, c.NOSLIDES, c.NOFS,
		c.VALUE1, c.VALUE2, c.VALUE3, c.VALUE4, c.FINALED,
		c.GROSSTAT, c.ROUTETAT, c.FINALTAT, c.TOTALTAT,
		c.CASENO, f.CODE AS FACI, b.SUBINIT, p.INITIALS, s.SPYNAME, m.CODE AS SPEC
	FROM dbo.Cases AS c
	INNER JOIN dbo.Facilities AS f ON f.FACID = c.FACID
	INNER JOIN dbo.Specialties AS s ON s.SPYID = c.SPYID
	INNER JOIN dbo.Subspecial AS b ON b.SUBID = c.SUBID
	INNER JOIN dbo.MasterSpecimens AS m ON m.MSID = c.MSID
	INNER JOIN dbo.Personnel AS p ON p.PERID = c.FINALID;
GO

CREATE VIEW [dbo].[udvDashboard]
AS
	SELECT p.CASEID, p.FACID, p.GROSSTAT, p.EMBEDTAT,
		p.MICROTAT, p.STAINTAT, p.ROUTETAT, p.FINALTAT, p.GROSSID,
		p.EMBEDID, p.MICROID, p.STAINID, p.ROUTEID, p.FINALID, p.SUBID,
		p.PROID, p.STATUS, p.NOSPECS, p.NOBLOCKS, p.NOSLIDES, p.ACCESSED,
		p.GROSSED, p.EMBEDED, p.MICROED, p.STAINED, p.ROUTED, p.FINALED,
		p.CASENO, s.SUBINIT, m.CODE, m.GROSS, m.EMBED, m.MICROTOMY,
		m.ROUTE, m.SIGNOUT, p1.INITIALS AS GROSSINI,
		p2.INITIALS AS EMBEDINI, p3.INITIALS AS MICROINI,
		p4.INITIALS AS STAININI, p5.INITIALS AS ROUTEINI,
		p6.INITIALS AS FINALINI
	FROM dbo.Pending AS p
	INNER JOIN dbo.Subspecial AS s ON s.SUBID = p.SUBID
	INNER JOIN dbo.MasterSpecimens AS m ON m.MSID = p.MSID
	INNER JOIN dbo.Personnel AS p1 ON p1.PERID = p.GROSSID
	INNER JOIN dbo.Personnel AS p2 ON p2.PERID = p.EMBEDID
	INNER JOIN dbo.Personnel AS p3 ON p3.PERID = p.MICROID

	INNER JOIN dbo.Personnel AS p4 ON p4.PERID = p.STAINID
	INNER JOIN dbo.Personnel AS p5 ON p5.PERID = p.ROUTEID
	INNER JOIN dbo.Personnel AS p6 ON p6.PERID = p.FINALID;
GO

CREATE VIEW [dbo].[udvDashLastRun]
AS
	SELECT MAX(ACCESSED) AS accession
	FROM dbo.Pending;
GO

CREATE VIEW [dbo].[udvGroups]
AS
	SELECT g.ID, g.GRP, g.CODE1, g.CODE2, g.CODE3,
		g.CODE4, g.NAME, c1.NAME AS NAME1, c2.NAME AS NAME2,
		c3.NAME AS NAME3, c4.NAME AS NAME4
	FROM dbo.Groups AS g
	INNER JOIN dbo.Coder1 AS c1 ON c1.ID = g.CODE1
	INNER JOIN dbo.Coder2 AS c2 ON c2.ID = g.CODE2
	INNER JOIN dbo.Coder3 AS c3 ON c3.ID = g.CODE3
	INNER JOIN dbo.Coder4 AS c4 ON c4.ID = g.CODE4;
GO

CREATE VIEW [dbo].[udvFrozens]
AS
	SELECT r.CASEID, r.PERID, r.NOSPECS, r.NOBLOCKS, r.NOSLIDES,
		r.VALUE1, r.VALUE2, r.VALUE3, r.VALUE4, c.FACID,
		c.SPYID, c.SUBID, c.NOFS, c.ACCESSED, c.CASENO,
		c.PROID, f.CODE, s.SPYNAME, b.SUBINIT, p.INITIALS
	FROM dbo.Frozens AS r
       INNER JOIN dbo.Cases AS c ON c.CASEID = r.CASEID
       INNER JOIN dbo.Facilities AS f ON f.FACID = c.FACID
       INNER JOIN dbo.Personnel AS p ON p.PERID = r.PERID
       INNER JOIN dbo.Specialties AS s ON s.SPYID = c.SPYID
       INNER JOIN dbo.Subspecial AS b ON b.SUBID = c.SUBID
GO

CREATE VIEW [dbo].[udvMasterOrders]
AS
	SELECT o.ID, o.GRPID, o.CODE, o.DESCR, g.GRP,
		g.NAME AS NAMEG, c1.NAME AS NAME1, c2.NAME AS NAME2,
		c3.NAME AS NAME3, c4.NAME AS NAME4
	FROM dbo.MasterOrders AS o
	INNER JOIN dbo.Groups AS g ON g.ID = o.GRPID

	INNER JOIN dbo.Coder1 AS c1 ON c1.ID = g.CODE1
	INNER JOIN dbo.Coder2 AS c2 ON c2.ID = g.CODE2
	INNER JOIN dbo.Coder3 AS c3 ON c3.ID = g.CODE3
	INNER JOIN dbo.Coder4 AS c4 ON c4.ID = g.CODE4;
GO

CREATE VIEW [dbo].[udvMasterSpec]
AS
	SELECT m.MSID, m.SPYID, m.SUBID, m.PROCID,
		m.ISLN, m.GROSS, m.EMBED, m.MICROTOMY, m.ROUTE, m.SIGNOUT, m.CODE1B,
		m.CODE1M, m.CODE1R, m.CODE2B, m.CODE2M, m.CODE2R, m.CODE3B, m.CODE3M,
		m.CODE3R, m.CODE4B, m.CODE4M, m.CODE4R, m.CODE, m.DESCR, s.SPYNAME,
		b.SUBNAME, c1b.NAME AS CODE1NB, c1m.NAME AS CODE1NM, c1r.NAME AS CODE1NR,
		c2b.NAME AS CODE2NB, c2m.NAME AS CODE2NM, c2r.NAME AS CODE2NR,
		c3b.NAME AS CODE3NB, c3m.NAME AS CODE3NM, c3r.NAME AS CODE3NR,
		c4b.NAME AS CODE4NB, c4m.NAME AS CODE4NM, c4r.NAME AS CODE4NR
	FROM dbo.MasterSpecimens AS m
	INNER JOIN dbo.Coder1 AS c1b ON c1b.ID = m.CODE1B
	INNER JOIN dbo.Coder2 AS c2b ON c2b.ID = m.CODE2B
	INNER JOIN dbo.Coder3 AS c3b ON c3b.ID = m.CODE3B
	INNER JOIN dbo.Coder4 AS c4b ON c4b.ID = m.CODE4B
	INNER JOIN dbo.Coder1 AS c1m ON c1m.ID = m.CODE1M
	INNER JOIN dbo.Coder2 AS c2m ON c2m.ID = m.CODE2M
	INNER JOIN dbo.Coder3 AS c3m ON c3m.ID = m.CODE3M
	INNER JOIN dbo.Coder4 AS c4m ON c4m.ID = m.CODE4M
	INNER JOIN dbo.Coder1 AS c1r ON c1r.ID = m.CODE1R
	INNER JOIN dbo.Coder2 AS c2r ON c2r.ID = m.CODE2R
	INNER JOIN dbo.Coder3 AS c3r ON c3r.ID = m.CODE3R
	INNER JOIN dbo.Coder4 AS c4r ON c4r.ID = m.CODE4R
	INNER JOIN dbo.Specialties AS s ON s.SPYID = m.SPYID
	INNER JOIN dbo.Subspecial AS b ON b.SUBID = m.SUBID;
GO

CREATE VIEW [dbo].[udvOrders]
AS
	SELECT o.SPECID, o.QTY, o.VALUE1, o.VALUE2, o.VALUE3, o.VALUE4, g.NAME
	FROM dbo.Orders AS o
	INNER JOIN dbo.Groups AS g ON g.ID = o.GRPID;
GO

CREATE VIEW [dbo].[udvSpecimens]
AS
SELECT s.SPECID, s.CASEID, s.MSID, s.NOBLOCKS, s.NOSLIDES, s.NOFRAGS, s.VALUE1, s.VALUE2,
	s.VALUE3, s.VALUE4, s.DESCR, m.CODE, m.DESCR AS MDESCR, c.FACID, c.SPYID, 
	c.SUBID, c.FINALED, c.CASENO
FROM dbo.Specimens AS s 
INNER JOIN dbo.MasterSpecimens AS m ON m.MSID = s.MSID
INNER JOIN dbo.Cases AS c ON c.CASEID = s.CASEID
GO

CREATE VIEW [dbo].[udvStats]
AS
	SELECT s.CASEID, s.FACID, s.SPYID, s.SUBID, s.PROID, s.FINALID,
		s.NOSPECS, s.NOBLOCKS, s.NOSLIDES, s.NOHE, s.noSS, s.NOIHC,
		s.NOMOL, s.NOFSP, s.NOFBL, s.NOFSL, s.NOSYN, s.GRTAT,
		s.ROTAT, s.FITAT, s.TOTAT, s.FINALED, s.CASENO,
		f.CODE AS FACI, y.SPYNAME, b.SUBINIT, p.PLAST, m.CODE AS SPEC
	FROM dbo.Stats AS s
	INNER JOIN dbo.Facilities AS f ON f.FACID = s.FACID
	INNER JOIN dbo.Specialties AS y ON y.SPYID = s.SPYID
	INNER JOIN dbo.Subspecial AS b ON b.SUBID = s.SUBID
	INNER JOIN dbo.Personnel AS p ON p.PERID = s.FINALID
	INNER JOIN dbo.MasterSpecimens AS m ON m.MSID = s.MSID;
GO

CREATE VIEW [dbo].[udvStatsLastRun]
AS
	SELECT MAX(FINALED) AS finaled 
	FROM dbo.Stats;
GO

CREATE VIEW [dbo].[udvSubspecial]
AS
	SELECT b.SUBID, b.SPYID, b.SUBINIT, b.SUBNAME, s.SPYNAME
	FROM Subspecial AS b
	INNER JOIN Specialties AS s ON s.SPYID = b.SPYID;
GO

CREATE VIEW [dbo].[udvWloadLastRun]
AS
	SELECT MAX(FINALED) AS finaled 
	FROM dbo.Cases;
GO

-- -----------------------------------------------------
-- Create Procedures
-- -----------------------------------------------------

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[udpAccessionsName]
AS
BEGIN
	SET NOCOUNT ON;
	SELECT ACCID, SPYID, DASH, WLOAD, ACCNAME,
		CODESPEC, SDASH, SWLOAD, SPYNAME
	FROM dbo.udvAccessions
	ORDER BY ACCNAME;
END
GO

CREATE PROCEDURE [dbo].[udpAdditionalCaseID] (@p1 int)
AS
BEGIN
	SET NOCOUNT ON;
	SELECT FINALED, PERID, CODEID, CASENO
	FROM dbo.udvAdditional
	WHERE CASEID = @p1
	ORDER BY FINALED;
END
GO

CREATE PROCEDURE [dbo].[udpAdditionalsCaseID] (@p int)
AS
BEGIN
	SET NOCOUNT ON;
	SELECT PERID, CODEID, VALUE1, VALUE2,
	VALUE3, VALUE4, FINALED, INITIALS
	FROM dbo.udvAdditional
	WHERE CASEID =  @p
	ORDER BY FINALED;
END
GO

CREATE PROCEDURE [dbo].[udpAdditionalSum] (@p1 datetime, @p2 datetime)
AS
BEGIN
	SET NOCOUNT ON;
	SELECT FACID, SPYID, SUBID, PERID,
		CODE, SPYNAME, SUBINIT, INITIALS,
		SUM(VALUE1) AS VALUE1, SUM(VALUE2) AS VALUE2,
		SUM(VALUE3) AS VALUE3, SUM(VALUE4) AS VALUE4
	FROM dbo.udvAdditional
	WHERE FINALED BETWEEN @p1 AND @p2
	GROUP BY FACID, SPYID, SUBID, PERID, CODE, SPYNAME, SUBINIT, INITIALS
	ORDER BY CODE, SPYID, SUBID, PERID;
END
GO

CREATE PROCEDURE [dbo].[udpCasesSum] (@p1 datetime, @p2 datetime)
AS
BEGIN
	SET NOCOUNT ON;
	SELECT FACID, SPYID, SUBID, FINALID,
		FACI, SPYNAME, SUBINIT, INITIALS,
		COUNT(CASEID) AS NOCASES,
		SUM(NOSLIDES) AS NOSLIDES,
		SUM(VALUE1) AS VALUE1,
		SUM(VALUE2) AS VALUE2,
		SUM(VALUE3) AS VALUE3,
		SUM(VALUE4) AS VALUE4
	FROM dbo.udvCases
	WHERE FINALED BETWEEN @p1 AND @p2
	GROUP BY FACID, SPYID, SUBID, FINALID,
		FACI, SPYNAME, SUBINIT, INITIALS
	ORDER BY FACI, SPYID, SUBID, FINALID;
END
GO

CREATE PROCEDURE [dbo].[udpCommentsCaseID] (@p1 int)
AS
BEGIN
	SET NOCOUNT ON;
	SELECT COMMENT FROM dbo.Comments WHERE CASEID = @p1;
END
GO

CREATE PROCEDURE [dbo].[udpDashboardCaseID]
AS
BEGIN
	SET NOCOUNT ON;
	SELECT *
	FROM dbo.udvDashboard
	ORDER BY CASEID;
END
GO

CREATE PROCEDURE [dbo].[udpDashboardPending]
AS
BEGIN
	SET NOCOUNT ON;
	SELECT CASEID, FACID, MSID, GROSSTAT, EMBEDTAT, MICROTAT,
		STAINTAT, ROUTETAT, HISTOTAT, FINALTAT, GROSSID, EMBEDID,
		MICROID, STAINID, ROUTEID, FINALID, SPYID, SUBID, PROID,
		STATUS, NOSPECS, NOBLOCKS, NOSLIDES, ACCESSED, GROSSED,
		EMBEDED, MICROED, STAINED, ROUTED, FINALED, CASENO
	FROM dbo.Pending
	WHERE STATUS < 7
	ORDER BY CASEID;
END
GO

CREATE PROCEDURE [dbo].[udpFrozensCaseID] (@p int)
AS
BEGIN
	SET NOCOUNT ON;

	SELECT f.PERID, f.NOSPECS, f.NOBLOCKS, f.NOSLIDES, f.VALUE1, f.VALUE2,
		f.VALUE3, f.VALUE4, c.ACCESSED, p.INITIALS
	FROM Frozens AS f
	INNER JOIN Cases AS c ON c.CASEID = f.CASEID
	INNER JOIN Personnel AS p ON p.PERID = f.PERID
	WHERE f.CASEID = @p;
END
GO

CREATE PROCEDURE [dbo].[udpFrozensSum] (@p1 datetime, @p2 datetime)
AS
BEGIN
	SET NOCOUNT ON;
	SELECT FACID, SPYID, SUBID, PERID,
		CODE, SPYNAME, SUBINIT, INITIALS,
		COUNT(CASEID) AS NOCASES,
		SUM(NOSLIDES) AS NOSLIDES,
		SUM(VALUE1) AS VALUE1,
		SUM(VALUE2) AS VALUE2,
		SUM(VALUE3) AS VALUE3,
		SUM(VALUE4) AS VALUE4
	FROM dbo.udvFrozens
	WHERE ACCESSED BETWEEN @p1 AND @p2
	GROUP BY FACID, SPYID, SUBID, PERID,
		CODE, SPYNAME, SUBINIT, INITIALS
	ORDER BY CODE, SPYID, SUBID, PERID;
END
GO

CREATE PROCEDURE [dbo].[udpGroupsID]
AS
BEGIN
	SET NOCOUNT ON;
	SELECT *
	FROM dbo.udvGroups
	ORDER BY ID;
END
GO

CREATE PROCEDURE [dbo].[udpGroupsName]
AS
BEGIN
	SET NOCOUNT ON;
	SELECT *
	FROM dbo.udvGroups
	ORDER BY NAME;
END
GO

CREATE PROCEDURE [dbo].[udpMasterSpecID]
AS
BEGIN
	SET NOCOUNT ON;
	SELECT *
	FROM dbo.udvMasterSpec
	ORDER BY MSID;
END
GO

CREATE PROCEDURE [dbo].[udpMasterSpecName]
AS
BEGIN
	SET NOCOUNT ON;
	SELECT *
	FROM dbo.udvMasterSpec
	ORDER BY CODE;
END
GO

CREATE PROCEDURE [dbo].[udpOrdersSpecID] (@p1 int)
AS
BEGIN
	SET NOCOUNT ON;
	SELECT *
	FROM dbo.udvOrders
	WHERE SPECID = @p1
	ORDER BY NAME;
END
GO

CREATE PROCEDURE [dbo].[udpPathologists]
AS
BEGIN
	SET NOCOUNT ON;
	SELECT PERID, INITIALS 
	FROM Personnel
	WHERE CODE = 'PT' 
	ORDER BY INITIALS;
END
GO

CREATE PROCEDURE [dbo].[udpSpecimensCaseID] (@p1 int)
AS
BEGIN
	SET NOCOUNT ON;
	SELECT SPECID, MSID, NOBLOCKS, NOSLIDES, NOFRAGS, VALUE1,
		VALUE2, VALUE3, VALUE4, DESCR, CODE, FINALED, CASENO
	FROM dbo.udvSpecimens
	WHERE CASEID =  @p1
	ORDER BY SPECID;
END
GO

CREATE PROCEDURE [dbo].[udpSubspecialID]
AS
BEGIN
	SET NOCOUNT ON;
	SELECT SUBID, SPYID, SUBINIT, SUBNAME, SPYNAME
	FROM udvSubspecial
	ORDER BY SUBID;
END
GO

CREATE PROCEDURE [dbo].[udpSubspecialName]
AS
BEGIN
	SET NOCOUNT ON;
	SELECT SUBID, SPYID, SUBINIT, SUBNAME, SPYNAME
	FROM udvSubspecial
	ORDER BY SUBNAME;
END
GO

CREATE PROCEDURE [dbo].[udpStatsSum] (@p1 datetime, @p2 datetime)
AS
BEGIN
	SET NOCOUNT ON;
	SELECT FACID, SPYID, SUBID, PROID, FINALID, FACI,
		SPYNAME, SUBINIT, PLAST, SUM(CASEID) AS NOCASES,
		SUM(NOSPECS) AS NOSPECS, SUM(NOBLOCKS) AS NOBLOCKS,
		SUM(NOSLIDES) AS NOSLIDES, SUM(NOHE) AS NOHE, SUM(noSS) AS noSS,
		SUM(NOIHC) AS NOIHC, SUM(NOMOL) AS NOMOL, SUM(NOFSP) AS NOFSP,
		SUM(NOFBL) AS NOFBL, SUM(NOFSL) AS NOFSL, SUM(NOSYN) AS NOSYN,
		SUM(GRTAT) AS GRTAT, SUM(ROTAT) AS ROTAT, SUM(FITAT) AS FITAT,
		SUM(TOTAT) AS TOTAT
	FROM dbo.udvStats
	WHERE FINALED BETWEEN @p1 AND @p2
	GROUP BY FACID, SPYID, SUBID, PROID, FINALID,
		FACI, SPYNAME, SUBINIT, PLAST
	ORDER BY FACI, SPYID, SUBID, PROID, FINALID;
END
GO

CREATE PROCEDURE [dbo].[udpTATSum] (@p1 datetime, @p2 datetime)
AS
BEGIN
	SET NOCOUNT ON;
	SELECT FACID, SPYID, SUBID, PROID, FINALID,
		FACI, SPYNAME, SUBINIT, INITIALS,
		COUNT(CASEID) AS NOCASES,
		SUM(GROSSTAT) AS GROSS,
		SUM(ROUTETAT) AS ROUTE,
		SUM(FINALTAT) AS FINAL,
		SUM(TOTALTAT) AS TOTAL
	FROM dbo.udvCases
	WHERE FINALED BETWEEN @p1 AND @p2
	GROUP BY FACID, SPYID, SUBID, PROID, FINALID,
		FACI, SPYNAME, SUBINIT, INITIALS
	ORDER BY FACI, SPYID, SUBID, FINALID;
END
GO

CREATE PROCEDURE [dbo].[udpErrorsCaseID] (@p1 int)
AS
BEGIN
	SET NOCOUNT ON;
	SELECT COMMENT FROM dbo.Errors WHERE CASEID = @p1;
END
GO

CREATE PROCEDURE [dbo].[udpErrorNZ]
AS
BEGIN
	SET NOCOUNT ON;
	SELECT CASEID, ERRID, CASENO
	FROM dbo.Errors
	WHERE ERRID > 0
	ORDER BY CASENO;
END
GO

CREATE PROCEDURE [dbo].[udpErrorZ]
AS
BEGIN
	SET NOCOUNT ON;
	SELECT CASEID, ERRID, CASENO
	FROM dbo.Errors
	WHERE ERRID = 0
	ORDER BY CASEID;
END
GO

