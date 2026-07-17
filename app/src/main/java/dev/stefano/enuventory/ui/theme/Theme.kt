@file:Suppress("unused")
package dev.stefano.enuventory.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

class EnuColors(
    val backgroundBrandPrimaryStrongDefault: Color,
    val backgroundDisabled: Color,
    val backgroundPrimaryStrongPressed: Color,
    val backgroundSignalErrorMediumDefault: Color,
    val backgroundSignalSuccessMediumDefault: Color,
    val backgroundSignalWarningMediumDefault: Color,
    val backgroundSignalWarningStrongDefault: Color,
    val borderDefaultMedium: Color,
    val contentBrandPrimaryDefault: Color,
    val contentBrandPrimaryOnStrong: Color,
    val contentDefaultDisabled: Color,
    val contentDefaultInversePrimary: Color,
    val contentDefaultPrimary: Color,
    val contentDefaultSubtle: Color,
    val contentSignalErrorDefault: Color,
    val contentSignalErrorOnSubtle: Color,
    val contentSignalSuccessDefault: Color,
    val contentSignalSuccessOnSubtle: Color,
    val contentSignalWarningOnSubtle: Color,
    val surfaceBrandPrimaryStrong: Color,
    val surfaceDefaultBase: Color,
    val surfaceDefaultLevel3: Color,

    val backgroundNeutralMediumDefault: Color,
    val contentSignalWarningDefault: Color,
)

val EnuLightColors = EnuColors(
    backgroundBrandPrimaryStrongDefault = BaseBrandPrimary500,
    backgroundDisabled = BaseNeutralBlack10,
    backgroundPrimaryStrongPressed = BaseBrandPrimary700,
    backgroundSignalErrorMediumDefault = BaseSignalError200,
    backgroundSignalSuccessMediumDefault = BaseSignalSuccess200,
    backgroundSignalWarningMediumDefault = BaseSignalWarning200,
    backgroundSignalWarningStrongDefault = BaseSignalWarning500,
    borderDefaultMedium = BaseNeutralGrey200,
    contentBrandPrimaryDefault = BaseBrandPrimary600,
    contentBrandPrimaryOnStrong = BaseNeutralWhite100,
    contentDefaultDisabled = BaseNeutralGrey400,
    contentDefaultInversePrimary = BaseNeutralWhite100,
    contentDefaultPrimary = BaseNeutralBlack100,
    contentDefaultSubtle = BaseNeutralGrey600,
    contentSignalErrorDefault = BaseSignalError600,
    contentSignalErrorOnSubtle = BaseSignalError800,
    contentSignalSuccessDefault = BaseSignalSuccess600,
    contentSignalSuccessOnSubtle = BaseSignalSuccess800,
    contentSignalWarningOnSubtle = BaseSignalWarning800,
    surfaceDefaultBase = BaseNeutralGrey50,
    surfaceDefaultLevel3 = BaseNeutralGrey100,

    surfaceBrandPrimaryStrong = BaseBrandPrimary500,
    backgroundNeutralMediumDefault = BaseNeutralBlack10,
    contentSignalWarningDefault = BaseSignalWarning600,
)

val EnuDarkColors = EnuColors(
    backgroundBrandPrimaryStrongDefault = BaseBrandPrimary500,
    backgroundDisabled = BaseNeutralWhite10,
    backgroundPrimaryStrongPressed = BaseBrandPrimary300,
    backgroundSignalErrorMediumDefault = BaseSignalError700,
    backgroundSignalSuccessMediumDefault = BaseSignalSuccess700,
    backgroundSignalWarningMediumDefault = BaseSignalWarning700,
    backgroundSignalWarningStrongDefault = BaseSignalWarning500,
    borderDefaultMedium = BaseNeutralGrey200,
    contentBrandPrimaryDefault = BaseBrandPrimary600,
    contentBrandPrimaryOnStrong = BaseNeutralWhite100,
    contentDefaultDisabled = BaseNeutralGrey600,
    contentDefaultInversePrimary = BaseNeutralBlack100,
    contentDefaultPrimary = BaseNeutralWhite100,
    contentDefaultSubtle = BaseNeutralWhite80,
    contentSignalErrorDefault = BaseSignalError200,
    contentSignalErrorOnSubtle = BaseSignalError400,
    contentSignalSuccessDefault = BaseSignalSuccess600,
    contentSignalSuccessOnSubtle = BaseSignalSuccess400,
    contentSignalWarningOnSubtle = BaseSignalWarning400,
    surfaceDefaultBase = BaseNeutralGrey950,
    surfaceDefaultLevel3 = BaseNeutralGrey700,

    surfaceBrandPrimaryStrong = BaseBrandPrimary500,
    backgroundNeutralMediumDefault = BaseNeutralWhite10,
    contentSignalWarningDefault = BaseSignalWarning600,
)

// provide color globally in the compose tree
val LocalEnuColors = staticCompositionLocalOf<EnuColors> {
    error("No EnuColors provided. Make sure to wrap your content in EnuTheme {}")
}

// provide typography globally in the compose tree
val LocalEnuTypography = staticCompositionLocalOf<EnuTypography> {
    error("No EnuTypography provided")
}

@Composable
fun EnuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) EnuDarkColors else EnuLightColors

    CompositionLocalProvider(
        LocalEnuColors provides colors,
        LocalEnuTypography provides EnuTypographyStyle,
        content = content
    )
}


object EnuTheme {
    val colors: EnuColors
        @Composable
        get() = LocalEnuColors.current

    val typography: EnuTypography
        @Composable
        get() = LocalEnuTypography.current
}