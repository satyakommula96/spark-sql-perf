#!/bin/bash
set -euo pipefail

echo "=== Installing TPC-DS, TPC-H, and spark-sql-perf ==="

# GCS Paths — FULL directories, not just binaries
TPCDS_BIN="gs://dp-tpc-dbgen-dbx/binaries/tpcds/dsdgen_bin/dsdgen"
TPCDS_IDX="gs://dp-tpc-dbgen-dbx/binaries/tpcds/dsdgen_bin/tpcds.idx"

TPCH_BIN="gs://dp-tpc-dbgen-dbx/binaries/tpch/dbgen_bin/dbgen"
TPCH_DISTS="gs://dp-tpc-dbgen-dbx/binaries/tpch/dbgen_bin/dists.dss"

SPARK_SQL_PERF_JAR="gs://dp-tpc-dbgen-dbx/binaries/spark-sql-perf_2.12-0.5.2-SNAPSHOT.jar"

# Detect gsutil (supports Debian & Ubuntu images)
if command -v gsutil >/dev/null 2>&1; then
  GSUTIL="$(command -v gsutil)"
elif [ -x /usr/bin/gsutil ]; then
  GSUTIL="/usr/bin/gsutil"
elif [ -x /snap/bin/gsutil ]; then
  GSUTIL="/snap/bin/gsutil"
else
  echo "ERROR: gsutil not found." >&2
  exit 1
fi

echo "Using gsutil at: ${GSUTIL}"

# Install directories
INSTALL_BASE="/usr/local"
TPCDS_DIR="${INSTALL_BASE}/tpcds"
TPCH_DIR="${INSTALL_BASE}/tpch"
SPARK_JARS_DIR="/usr/lib/spark/jars"

mkdir -p "${TPCDS_DIR}"
mkdir -p "${TPCH_DIR}"

# Install TPC-DS
echo "Copying TPC-DS binary + metadata..."
"${GSUTIL}" cp "${TPCDS_BIN}" "${TPCDS_DIR}/dsdgen"
"${GSUTIL}" cp "${TPCDS_IDX}" "${TPCDS_DIR}/tpcds.idx"

chmod +x "${TPCDS_DIR}/dsdgen"
chmod 644 "${TPCDS_DIR}/tpcds.idx"

# Install TPC-H
echo "Copying TPC-H binary + metadata..."
"${GSUTIL}" cp "${TPCH_BIN}" "${TPCH_DIR}/dbgen"
"${GSUTIL}" cp "${TPCH_DISTS}" "${TPCH_DIR}/dists.dss"

chmod +x "${TPCH_DIR}/dbgen"
chmod 644 "${TPCH_DIR}/dists.dss"

# Grant global permissions so Spark's yarn user can execute/read/write in these directories
chmod -R 777 "${TPCDS_DIR}" "${TPCH_DIR}"

# Install spark-sql-perf JAR
echo "Installing spark-sql-perf jar..."
JAR_FILENAME=$(basename "${SPARK_SQL_PERF_JAR}")

# Ensure the jars directory exists and clean up any old versions to avoid classpath conflicts
mkdir -p "${SPARK_JARS_DIR}"
rm -f "${SPARK_JARS_DIR}"/spark-sql-perf*.jar

"${GSUTIL}" cp "${SPARK_SQL_PERF_JAR}" "${SPARK_JARS_DIR}/${JAR_FILENAME}"
chmod 644 "${SPARK_JARS_DIR}/${JAR_FILENAME}"

# PATH update
echo "export PATH=\$PATH:${TPCDS_DIR}:${TPCH_DIR}" \
  > /etc/profile.d/tpcds_tpch.sh

echo "=== Installation complete ==="

