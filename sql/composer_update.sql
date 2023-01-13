\set ON_ERROR_STOP

-- This SQL script will insert msg_pattern records for the v5.37 composer
-- update.
-- NOTE: this uses MnDOT's naming scheme; changes needed for other agencies.
-- Do not run this script while the IRIS server is online.  Also, please backup
-- the database before running this script.
--
-- psql tms -f composer_update.sql

SET SESSION AUTHORIZATION 'tms';
BEGIN;

INSERT INTO iris.msg_pattern (name, sign_config, sign_group, multi)
    (SELECT 'OPER_' || substring(sign_config FROM 4), sign_config, 'STD_8CH',
            '[np]'
     FROM dms_view dv JOIN dms_sign_group_view sg ON dv.name = sg.dms
     WHERE sg.sign_group = 'STD_8CH' AND sign_config IS NOT NULL
     GROUP BY sign_config);

INSERT INTO iris.msg_pattern (name, sign_config, sign_group, multi)
    (SELECT 'OPER_' || substring(sign_config FROM 4), sign_config,
            'MAINT_WARN_2LN', '[np]'
     FROM dms_view dv JOIN dms_sign_group_view sg ON dv.name = sg.dms
     WHERE sg.sign_group = 'MAINT_WARN_2LN' AND sign_config IS NOT NULL
     GROUP BY sign_config);

INSERT INTO iris.msg_pattern (name, sign_config, sign_group, multi)
    (SELECT 'OPER_' || substring(sign_config FROM 4), sign_config,
            'EZPASS_370WIDE', '[tr156,1,214,56]'
     FROM dms_view dv JOIN dms_sign_group_view sg ON dv.name = sg.dms
     WHERE sg.sign_group = 'EZPASS_370WIDE' AND sign_config IS NOT NULL
     GROUP BY sign_config);

INSERT INTO iris.msg_pattern (name, sign_config, sign_group, multi)
    (SELECT 'OPER_' || substring(sign_config FROM 4), sign_config,
            'EZPASS_592WIDE', '[tr243,1,350,96]'
     FROM dms_view dv JOIN dms_sign_group_view sg ON dv.name = sg.dms
     WHERE sg.sign_group = 'EZPASS_592WIDE' AND sign_config IS NOT NULL
     GROUP BY sign_config);

INSERT INTO iris.msg_pattern (name, sign_config, sign_group, multi)
    (SELECT 'OPER_' || substring(sign_config FROM 4), sign_config,
            'V94W08', '[tr1,1,224,70]'
     FROM dms_view dv JOIN dms_sign_group_view sg ON dv.name = sg.dms
     WHERE sg.sign_group = 'V94W08' AND sign_config IS NOT NULL
     GROUP BY sign_config);

UPDATE iris.msg_pattern
     SET multi = '[g12,272,1][tr1,1,224,70]'
     WHERE name = 'HURON_DEF';

UPDATE iris.msg_pattern
     SET multi = '[g12,272,1][tr1,1,224,70]ROAD WORK[nl]ON 394[nl]REDUCED TO 2 LANES'
     WHERE name = 'HURON_TEMP_DEF';

INSERT INTO iris.msg_pattern (name, sign_config, sign_group, multi)
    (SELECT 'OPER_GARAGE', sign_config, 'TAD_GARAGE',
            '[fo6][cf255,255,255]PARKING INFORMATION[tr1,12,150,44]'
     FROM dms_view dv JOIN dms_sign_group_view sg ON dv.name = sg.dms
     WHERE sg.sign_group = 'TAD_GARAGE' AND sign_config IS NOT NULL
     GROUP BY sign_config);

COMMIT;
