\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';
BEGIN;

-- Delete RWIS action tag system attributes
DELETE FROM iris.system_attribute WHERE name IN (
    'rwis_auto_max_dist_miles',
    'rwis_slippery_1_percent',
    'rwis_slippery_2_degrees',
    'rwis_slippery_3_percent',
    'rwis_windy_1_kph',
    'rwis_windy_2_kph',
    'rwis_visibility_1_m',
    'rwis_visibility_2_m',
    'rwis_flooding_1_mm',
    'rwis_flooding_2_mm'
);

COMMIT;
