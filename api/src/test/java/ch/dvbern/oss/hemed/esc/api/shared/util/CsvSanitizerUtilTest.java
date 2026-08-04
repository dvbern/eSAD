package ch.dvbern.oss.hemed.esc.api.shared.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CsvSanitizerUtilTest {

	@Test
	void sanitize_neutralizesEqualsFormula() {
		assertThat(CsvSanitizerUtil.sanitize("=1+1")).isEqualTo("'=1+1");
		assertThat(CsvSanitizerUtil.sanitize("=HYPERLINK(\"http://evil\")")).isEqualTo("'=HYPERLINK(\"http://evil\")");
		assertThat(CsvSanitizerUtil.sanitize("=cmd|'/c calc'!A1")).isEqualTo("'=cmd|'/c calc'!A1");
	}

	@Test
	void sanitize_neutralizesPlusFormula() {
		assertThat(CsvSanitizerUtil.sanitize("+1+1")).isEqualTo("'+1+1");
		assertThat(CsvSanitizerUtil.sanitize("+cmd|'/c calc'!A1")).isEqualTo("'+cmd|'/c calc'!A1");
	}

	@Test
	void sanitize_neutralizesMinusFormula() {
		assertThat(CsvSanitizerUtil.sanitize("-1+1")).isEqualTo("'-1+1");
		assertThat(CsvSanitizerUtil.sanitize("-2+3+cmd")).isEqualTo("'-2+3+cmd");
		assertThat(CsvSanitizerUtil.sanitize("-A1")).isEqualTo("'-A1");
		assertThat(CsvSanitizerUtil.sanitize("-cmd|'/c calc'!A1")).isEqualTo("'-cmd|'/c calc'!A1");
	}

	@Test
	void sanitize_leavesNegativeNumbersAndPlaceholderDashUnchanged() {
		assertThat(CsvSanitizerUtil.sanitize("-")).isEqualTo("-");
		assertThat(CsvSanitizerUtil.sanitize("-42")).isEqualTo("-42");
		assertThat(CsvSanitizerUtil.sanitize("-123 456")).isEqualTo("-123 456");
	}

	@Test
	void sanitize_neutralizesAtFormula() {
		assertThat(CsvSanitizerUtil.sanitize("@SUM(A1:A2)")).isEqualTo("'@SUM(A1:A2)");
		assertThat(CsvSanitizerUtil.sanitize("@echo off")).isEqualTo("'@echo off");
	}

	@Test
	void sanitize_neutralizesTabAndCr() {
		assertThat(CsvSanitizerUtil.sanitize("\t=evil")).isEqualTo("'\t=evil");
		assertThat(CsvSanitizerUtil.sanitize("\rinjection")).isEqualTo("'\rinjection");
	}

	@Test
	void sanitize_leavesSafeValuesUnchanged() {
		assertThat(CsvSanitizerUtil.sanitize("Max")).isEqualTo("Max");
		assertThat(CsvSanitizerUtil.sanitize("Müller, Thomas")).isEqualTo("Müller, Thomas");
		assertThat(CsvSanitizerUtil.sanitize("756.1234.5678.90")).isEqualTo("756.1234.5678.90");
		assertThat(CsvSanitizerUtil.sanitize("Hauptstrasse 5")).isEqualTo("Hauptstrasse 5");
		assertThat(CsvSanitizerUtil.sanitize("normal text with spaces")).isEqualTo("normal text with spaces");
	}

	@Test
	void sanitize_doesNotModifyInternalSpecialChars() {
		assertThat(CsvSanitizerUtil.sanitize("a=b")).isEqualTo("a=b");
		assertThat(CsvSanitizerUtil.sanitize("email@test.ch")).isEqualTo("email@test.ch");
	}

	@Test
	void sanitize_leavesPhoneNumbersUnchanged() {
		assertThat(CsvSanitizerUtil.sanitize("+41 61 123 45 67")).isEqualTo("+41 61 123 45 67");
		assertThat(CsvSanitizerUtil.sanitize("+41791234567")).isEqualTo("+41791234567");
	}

	@Test
	void sanitize_stillNeutralizesPhoneLookalikesWithPayloadChars() {
		assertThat(CsvSanitizerUtil.sanitize("+41 61 123 45 67+cmd")).isEqualTo("'+41 61 123 45 67+cmd");
		assertThat(CsvSanitizerUtil.sanitize("+41-61-123")).isEqualTo("'+41-61-123");
		assertThat(CsvSanitizerUtil.sanitize("+")).isEqualTo("'+");
		assertThat(CsvSanitizerUtil.sanitize("+A1")).isEqualTo("'+A1");
	}

	@Test
	void sanitize_handlesNullAndEmpty() {
		assertThat(CsvSanitizerUtil.sanitize(null)).isEmpty();
		assertThat(CsvSanitizerUtil.sanitize("")).isEmpty();
	}
}
