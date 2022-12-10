package com.rrat.stopwatch

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rrat.stopwatch.service.ServiceHelper
import com.rrat.stopwatch.service.StopWatchService
import com.rrat.stopwatch.service.StopWatchState
import com.rrat.stopwatch.ui.theme.Blue
import com.rrat.stopwatch.ui.theme.Dark
import com.rrat.stopwatch.ui.theme.Light
import com.rrat.stopwatch.util.Constants.ACTION_SERVICE_CANCEL
import com.rrat.stopwatch.util.Constants.ACTION_SERVICE_START
import com.rrat.stopwatch.util.Constants.ACTION_SERVICE_STOP

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    stopWatchService: StopWatchService
){

    val context = LocalContext.current
    val hours by stopWatchService.hours
    val minutes by stopWatchService.minutes
    val seconds by stopWatchService.seconds
    val currentState by stopWatchService.currentState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Dark)
            .padding(30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.weight(weight = 9f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(targetState = hours, transitionSpec = { addAnimation()}) {
                TextTimeUnit(hours)
            }
            AnimatedContent(targetState = minutes, transitionSpec = { addAnimation()}) {
                TextTimeUnit(minutes)
            }
            AnimatedContent(targetState = seconds, transitionSpec = { addAnimation()}) {
                TextTimeUnit(seconds)
            }

        }

        Row(
            modifier = Modifier.weight(1f)
        ) {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.8f)
                ,
                onClick = {
                    ServiceHelper.triggerForegroundService(
                        context = context,
                        action = if(currentState == StopWatchState.Started) ACTION_SERVICE_STOP
                        else ACTION_SERVICE_START
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(currentState == StopWatchState.Started) Color.Red else Color.Blue,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if(currentState == StopWatchState.Started)
                        stringResource(id = R.string.stop) else stringResource(id = R.string.resume))
            }
            Spacer(modifier = Modifier.width(30.dp))
            Button(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.8f)
                ,
                onClick = {
                    ServiceHelper.triggerForegroundService(
                        context = context,
                        action = ACTION_SERVICE_CANCEL
                    )
                },
                enabled = seconds != "00" && currentState != StopWatchState.Started,
                colors = ButtonDefaults.buttonColors(disabledContainerColor = Light)
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }

    }

}

@Composable
private fun TextTimeUnit(
    text: String,
) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = MaterialTheme.typography.displayLarge.fontSize,
            fontWeight = FontWeight.Bold,
            color = if (text == "00" ) Color.White else Blue
        )
    )
}

@OptIn(ExperimentalAnimationApi::class)
fun addAnimation(duration: Int = 600) : ContentTransform{
    return slideInVertically(
        animationSpec = tween(durationMillis = duration))
    { height-> height } + fadeIn(animationSpec = tween(durationMillis = duration)
    ) with slideOutVertically(animationSpec = tween(durationMillis = duration))
    {height-> height}  + fadeOut(animationSpec = tween(durationMillis = duration))
}